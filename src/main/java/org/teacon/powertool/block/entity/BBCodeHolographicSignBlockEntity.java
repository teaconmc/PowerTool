package org.teacon.powertool.block.entity;

import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.PowerToolBlocks;

import java.util.*;
import java.util.concurrent.CompletableFuture;


public class BBCodeHolographicSignBlockEntity extends BaseHolographicSignBlockEntity {

    public static final NodeParser TPAPI_PARSER = TagParser.DEFAULT;

    /**
     * Represents text alignment for BBCodes and global default.
     */
    public enum TextAlign implements StringRepresentable {
        LEFT("left"),
        CENTER("center"),
        RIGHT("right");

        public static final TextAlign DEFAULT = CENTER;

        private final String serializedName;

        TextAlign(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public @NonNull String getSerializedName() {
            return serializedName;
        }

        public static Align toAlignEnum(TextAlign align) {
            return switch (align) {
                case LEFT -> Align.LEFT;
                case CENTER -> Align.CENTER;
                case RIGHT -> Align.RIGHT;
            };
        }
    }


    private enum ParserState {
        TEXT,
        TAG_START,
        TAG_NAME,
        TAG_PARAM,
        CLOSE_TAG,
        CLOSE_NAME
    }

    /**
     * 样式状态 - 用于保存和恢复嵌套标签的样式
     */
    private static class StyleState {
        Integer color;
        Integer backgroundColor;
        Float size;
        TextAlign align;
        Float lineHeight;
        Boolean bold;
        Boolean underline;
        Boolean italic;

        StyleState(Integer color, Integer backgroundColor, Float size, TextAlign align, Float lineHeight,
                   Boolean bold, Boolean underline, Boolean italic) {
            this.color = color;
            this.backgroundColor = backgroundColor;
            this.size = size;
            this.align = align;
            this.lineHeight = lineHeight;
            this.bold = bold;
            this.underline = underline;
            this.italic = italic;
        }
    }

    /**
     * 解析上下文 - 状态机的运行时上下文
     * 包含当前状态、缓冲区、样式状态等信息
     */
    private static class ParseContext {
        ParserState state = ParserState.TEXT;
        StringBuilder currentText = new StringBuilder();
        StringBuilder tagBuffer = new StringBuilder(); // 用于暂存标签名称或参数

        // 当前正在解析的标签信息
        String pendingTagName = null;
        String pendingTagParam = null;

        // 当前样式状态（使用数组以便在闭包中修改）
        Integer currentColor = null;
        Integer currentBackgroundColor = null;
        Float currentSize = null;
        TextAlign currentAlign = null;
        Float currentLineHeight = null;
        Boolean currentBold = null;
        Boolean currentUnderline = null;
        Boolean currentItalic = null;

        // 样式栈 - 用于处理嵌套标签
        Deque<StyleState> styleStack = new ArrayDeque<>();

        // 当前正在构建的行
        ParsedLine currentLine;

        // 解析结果
        List<ParsedLine> parsedLines = new ArrayList<>();

        // 全局默认值
        final TextAlign defaultAlign;
        final Float defaultLineHeight;

        ParseContext(TextAlign defaultAlign, Float defaultLineHeight) {
            this.defaultAlign = defaultAlign;
            this.defaultLineHeight = defaultLineHeight;
            this.currentLine = new ParsedLine(defaultAlign, defaultLineHeight);
        }

        /**
         * 将当前文本缓冲区的内容添加到当前行（使用当前样式）
         */
        void flushText() {
            if (!currentText.isEmpty()) {
                currentLine.addSegment(new TextSegment(
                        currentText.toString(),
                        currentColor, currentBackgroundColor, currentSize, currentAlign,
                        currentLineHeight, currentBold, currentUnderline, currentItalic));
                currentText = new StringBuilder();
            }
        }

        /**
         * 换行：结束当前行，开始新行
         */
        void newLine() {
            flushText();
            // 完成当前行（不添加换行符标记，因为 ParsedLine 已经完成行分割）
            currentLine.mergeSegments();
            parsedLines.add(currentLine);
            // 开始新行，保持当前样式状态
            currentLine = new ParsedLine(
                    currentAlign != null ? currentAlign : defaultAlign,
                    currentLineHeight != null ? currentLineHeight : defaultLineHeight
            );
        }

        /**
         * 应用一个打开标签，更新当前样式状态
         * 将当前状态压入栈中，以便后续关闭标签时恢复
         */
        boolean applyOpenTag(String tagName, String tagParam) {
            flushText(); // 先处理之前的文本

            // 保存当前状态到栈（在修改之前）
            styleStack.push(new StyleState(currentColor, currentBackgroundColor, currentSize, currentAlign,
                    currentLineHeight, currentBold, currentUnderline, currentItalic));

            switch (tagName.toLowerCase()) {
                case "color", "c" -> {
                    currentColor = parseColor(tagParam);
                    return true;
                }
                case "bg", "background" -> {
                    currentBackgroundColor = parseColor(tagParam);
                    return true;
                }
                case "size" -> {
                    try {
                        currentSize = Float.parseFloat(tagParam);
                        return true;
                    } catch (NumberFormatException e) {
                        currentSize = null;
                    }
                }
                case "align", "a" -> {
                    try {
                        currentAlign = TextAlign.valueOf(tagParam.toUpperCase());
                        return true;
                    } catch (IllegalArgumentException e) {
                        currentAlign = null;
                    }
                }
                case "lineheight", "lh" -> {
                    try {
                        currentLineHeight = Float.parseFloat(tagParam);
                        return true;
                    } catch (NumberFormatException e) {
                        currentLineHeight = null;
                    }
                }
                case "bold", "b" -> {
                    currentBold = true;
                    return true;
                }
                case "underline", "u" -> {
                    currentUnderline = true;
                    return true;
                }
                case "italic", "i" -> {
                    currentItalic = true;
                    return true;
                }
            }
            return false;
        }

        /**
         * 应用一个关闭标签，从栈中恢复之前的样式状态
         */
        void applyCloseTag(String tagName) {
            flushText(); // 先处理之前的文本

            // 从栈中恢复之前的样式状态
            if (!styleStack.isEmpty()) {
                StyleState prevState = styleStack.pop();

                switch (tagName.toLowerCase()) {
                    case "color", "c" -> currentColor = prevState.color;
                    case "bg", "background" -> currentBackgroundColor = prevState.backgroundColor;
                    case "size" -> currentSize = prevState.size;
                    case "align", "a" -> currentAlign = prevState.align;
                    case "lineheight", "lh" -> currentLineHeight = prevState.lineHeight;
                    case "bold", "b" -> currentBold = prevState.bold;
                    case "underline", "u" -> currentUnderline = prevState.underline;
                    case "italic", "i" -> currentItalic = prevState.italic;
                }
            } else {
                // 如果栈为空（异常情况），重置为 null
                switch (tagName.toLowerCase()) {
                    case "color", "c" -> currentColor = null;
                    case "bg", "background" -> currentBackgroundColor = null;
                    case "size" -> currentSize = null;
                    case "align", "a" -> currentAlign = null;
                    case "lineheight", "lh" -> currentLineHeight = null;
                    case "bold", "b" -> currentBold = null;
                    case "underline", "u" -> currentUnderline = null;
                    case "italic", "i" -> currentItalic = null;
                }
            }
        }
    }

    /**
     * Represents a styled text segment with all formatting properties.
     * Characters with the same style should be merged into one segment.
     *
     */
    public static final class TextSegment {
        private final String text;
        private final @Nullable Integer color;
        private final @Nullable Integer backgroundColor;
        private final @Nullable Float size;
        private final @Nullable TextAlign align;
        private final @Nullable Float lineHeight;
        private final @Nullable Boolean bold;
        private final @Nullable Boolean underline;
        private final @Nullable Boolean italic;

        /**
         * @param color           null means use default
         * @param backgroundColor null means use default (transparent)
         * @param size            null means use default
         * @param align           null means use default (affects entire line)
         * @param lineHeight      null means use default
         * @param bold            null means use default
         * @param underline       null means use default
         * @param italic          null means use default
         */
        public TextSegment(String text, @Nullable Integer color, @Nullable Integer backgroundColor,
                           @Nullable Float size, @Nullable TextAlign align,
                           @Nullable Float lineHeight, @Nullable Boolean bold, @Nullable Boolean underline,
                           @Nullable Boolean italic) {
            this.text = text;
            this.color = color;
            this.backgroundColor = backgroundColor;
            this.size = size;
            this.align = align;
            this.lineHeight = lineHeight;
            this.bold = bold;
            this.underline = underline;
            this.italic = italic;
        }

        /**
         * Check if this segment has the same style as another (ignoring text content).
         */
        public boolean sameStyle(@Nullable TextSegment other) {
            if (other == null) return false;
            return Objects.equals(this.color, other.color) &&
                    Objects.equals(this.backgroundColor, other.backgroundColor) &&
                    Objects.equals(this.size, other.size) &&
                    Objects.equals(this.align, other.align) &&
                    Objects.equals(this.lineHeight, other.lineHeight) &&
                    Objects.equals(this.bold, other.bold) &&
                    Objects.equals(this.underline, other.underline) &&
                    Objects.equals(this.italic, other.italic);
        }

        /**
         * Create a new segment with merged text from this and another segment with same style.
         */
        public TextSegment merge(TextSegment other) {
            if (!sameStyle(other)) return this;
            return new TextSegment(this.text + other.text, color, backgroundColor, size, align,
                    lineHeight, bold, underline, italic);
        }

        public String text() {
            return text;
        }

        public @Nullable Integer color() {
            return color;
        }

        public @Nullable Integer backgroundColor() {
            return backgroundColor;
        }

        public @Nullable Float size() {
            return size;
        }

        public @Nullable TextAlign align() {
            return align;
        }

        public @Nullable Float lineHeight() {
            return lineHeight;
        }

        public @Nullable Boolean bold() {
            return bold;
        }

        public @Nullable Boolean underline() {
            return underline;
        }

        public @Nullable Boolean italic() {
            return italic;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TextSegment) obj;
            return Objects.equals(this.text, that.text) &&
                    Objects.equals(this.color, that.color) &&
                    Objects.equals(this.backgroundColor, that.backgroundColor) &&
                    Objects.equals(this.size, that.size) &&
                    Objects.equals(this.align, that.align) &&
                    Objects.equals(this.lineHeight, that.lineHeight) &&
                    Objects.equals(this.bold, that.bold) &&
                    Objects.equals(this.underline, that.underline) &&
                    Objects.equals(this.italic, that.italic);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, color, backgroundColor, size, align, lineHeight, bold, underline, italic);
        }

        @Override
        public String toString() {
            return "TextSegment[" +
                    "text=" + text + ", " +
                    "color=" + color + ", " +
                    "backgroundColor=" + backgroundColor + ", " +
                    "size=" + size + ", " +
                    "align=" + align + ", " +
                    "lineHeight=" + lineHeight + ", " +
                    "bold=" + bold + ", " +
                    "underline=" + underline + ", " +
                    "italic=" + italic + ']';
        }

    }

    /**
     * Represents a parsed line with multiple text segments.
     */
    public static class ParsedLine {
        public final List<TextSegment> segments = new ArrayList<>();
        public TextAlign lineAlign; // alignment for the entire line
        public Float lineLineHeight; // line-height for this entire line

        public ParsedLine(TextAlign defaultAlign, Float defaultLineHeight) {
            this.lineAlign = defaultAlign;
            this.lineLineHeight = defaultLineHeight;
        }

        public void addSegment(TextSegment segment) {
            segments.add(segment);
            // If segment specifies alignment, update line alignment
            if (segment.align != null) {
                lineAlign = segment.align;
            }
            // If segment specifies line-height, update line line-height
            if (segment.lineHeight != null) {
                lineLineHeight = segment.lineHeight;
            }
        }

        /**
         * Merge consecutive segments with the same style.
         */
        public void mergeSegments() {
            List<TextSegment> merged = new ArrayList<>();
            for (var segment : segments) {
                if (!merged.isEmpty() && merged.getLast().sameStyle(segment)) {
                    merged.set(merged.size() - 1, merged.getLast().merge(segment));
                } else {
                    merged.add(segment);
                }
            }
            segments.clear();
            segments.addAll(merged);
        }
    }

    // Raw BBCodes content (one entry per line for editing)
    public List<String> rawContent = new ArrayList<>();

    // Client-side render cache (built by renderer)
    public Object clientCacheData = null;

    // Parsed for rendering (lines separated by \n in the original text)
    public List<ParsedLine> parsedLines = new ArrayList<>();

    // Filtered content after text filtering
    public List<Component> forRender = new ArrayList<>();

    // Global default parameters
    public int defaultColor = 0xFFFFFFFF; // ARGB format
    public float defaultScale = 1.0F;
    public TextAlign defaultAlign = TextAlign.DEFAULT;
    public float defaultLineHeight = 1.0F; // Line height multiplier
    public float minWidth = 0F; // Minimum width in blocks
    public boolean defaultBold = false;
    public boolean defaultUnderline = false;
    public boolean defaultItalic = false;

    public BBCodeHolographicSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.BBC_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    public void writeTo(@NonNull ValueOutput output) {
        super.writeTo(output);

        // 只写入原始内容和全局参数，parsedLines 可以在读取后重新解析

        // 写入原始内容（每行一个字符串）
        output.putInt("contentSize", rawContent.size());
        for (int i = 0; i < rawContent.size(); i++) {
            output.putString("content_" + i, rawContent.get(i));
        }

        // 写入全局默认参数
        output.putInt("defaultColor", defaultColor);
        output.putFloat("defaultScale", defaultScale);
        output.putInt("defaultAlign", defaultAlign.ordinal());
        output.putFloat("defaultLineHeight", defaultLineHeight);
        output.putFloat("minWidth", minWidth);
        output.putBoolean("defaultBold", defaultBold);
        output.putBoolean("defaultUnderline", defaultUnderline);
        output.putBoolean("defaultItalic", defaultItalic);
    }

    @Override
    public void readFrom(@NonNull ValueInput input) {
        super.readFrom(input);

        // 读取原始内容
        var contentSize = input.getIntOr("contentSize", 0);
        rawContent.clear();
        for (int i = 0; i < contentSize; i++) {
            rawContent.add(input.getStringOr("content_" + i, ""));
        }

        // 读取全局默认参数
        defaultColor = input.getIntOr("defaultColor", 0xFFFFFFFF);
        defaultScale = input.getFloatOr("defaultScale", 1.0F);
        int alignOrdinal = input.getIntOr("defaultAlign", TextAlign.DEFAULT.ordinal());
        defaultAlign = TextAlign.values()[alignOrdinal % TextAlign.values().length];
        defaultLineHeight = input.getFloatOr("defaultLineHeight", 1.0F);
        minWidth = input.getFloatOr("minWidth", 0F);
        defaultBold = input.getBooleanOr("defaultBold", false);
        defaultUnderline = input.getBooleanOr("defaultUnderline", false);
        defaultItalic = input.getBooleanOr("defaultItalic", false);

        // 更新基类属性
        this.colorInARGB = defaultColor;
        this.scale = defaultScale;
        this.align = TextAlign.toAlignEnum(defaultAlign);

        // 在客户端重新解析 BBCodes
        if (getLevel() != null && getLevel().isClientSide()) {
            // 重新构建 forRender 并解析
            forRender.clear();
            for (var rawLine : rawContent) {
                forRender.add(Component.literal(rawLine));
            }
            parseBBCode(forRender);
        }
    }

    @Override
    public void filterMessage(@NonNull ServerPlayer player) {
        forRender.clear();
        var taskList = new ArrayList<CompletableFuture<?>>();

        for (var rawLine : rawContent) {
            var task = player.getTextFilter().processStreamMessage(rawLine);
            task.thenAccept(filtered -> {
                if (player.isTextFilteringEnabled()) {
                    forRender.add(Component.literal(filtered.filteredOrEmpty()));
                } else {
                    forRender.add(Component.literal(filtered.raw()));
                }
            });
            taskList.add(task);
        }

        var finalTask = CompletableFuture.allOf(taskList.toArray(new CompletableFuture<?>[0]));
        finalTask.thenAcceptAsync((_) -> {
            parseBBCode(forRender);
            this.setChanged();
            if (level != null) {
                var state = this.getBlockState();
                level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            }
        }, player.server);
    }

    /**
     * Parse BBCodes from the content list.
     * All content is joined with \n and parsed as a single continuous text using a state machine.
     */
    public void parseBBCode(List<Component> content) {
        parsedLines.clear();

        // Join all content with \n to create a single text to parse
        StringBuilder fullText = new StringBuilder();
        for (int i = 0; i < content.size(); i++) {
            if (i > 0) {
                fullText.append("\n");
            }
            fullText.append(content.get(i).getString());
        }

        // Parse using state machine
        ParseContext ctx = new ParseContext(defaultAlign, defaultLineHeight);
        parseWithStateMachine(fullText.toString(), ctx);
        parsedLines = ctx.parsedLines;

        // Clear client cache so renderer will rebuild it
        clientCacheData = null;
    }

    /**
     * 使用状态机解析 BBCode 文本
     * <p>
     * 状态机逻辑：
     * <p>
     * TEXT 状态（默认）：
     * - '[' -> TAG_START（可能是标签开始）
     * - '\n' -> newLine()（换行）
     * - 其他 -> 累积文本
     * <p>
     * TAG_START 状态：
     * - '/' -> CLOSE_TAG（关闭标签）
     * - 字母 -> TAG_NAME（打开标签）
     * - 其他 -> 回到 TEXT（'[' 当作普通字符）
     * <p>
     * TAG_NAME 状态：
     * - '=' -> TAG_PARAM（有参数）
     * - ']' -> 应用标签，回到 TEXT
     * - 字母/数字 -> 继续读取标签名
     * - 其他 -> 回到 TEXT（无效标签）
     * <p>
     * TAG_PARAM 状态：
     * - ']' -> 应用标签（带参数），回到 TEXT
     * - 其他 -> 继续读取参数值
     * <p>
     * CLOSE_TAG 状态：
     * - 字母 -> CLOSE_NAME（读取关闭标签名）
     * - 其他 -> 回到 TEXT（无效的 '[/'）
     * <p>
     * CLOSE_NAME 状态：
     * - ']' -> 关闭标签，回到 TEXT
     * - 字母/数字 -> 继续读取标签名
     * - 其他 -> 回到 TEXT（无效关闭标签）
     */
    private void parseWithStateMachine(String text, ParseContext ctx) {
        if (text.isEmpty()) {
            return;
        }

        ctx.state = ParserState.TEXT;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            switch (ctx.state) {
                case TEXT -> {
                    // 默认状态：读取普通文本
                    if (c == '[') {
                        ctx.state = ParserState.TAG_START;
                        ctx.tagBuffer.setLength(0);
                    } else if (c == '\n') {
                        ctx.newLine();
                    } else {
                        ctx.currentText.append(c);
                    }
                }
                case TAG_START -> {
                    // 刚遇到 '['，判断是什么标签
                    if (c == '/') {
                        ctx.state = ParserState.CLOSE_TAG;
                        ctx.tagBuffer.setLength(0);
                    } else if (Character.isLetter(c)) {
                        ctx.state = ParserState.TAG_NAME;
                        ctx.tagBuffer.setLength(0);
                        ctx.tagBuffer.append(c);
                    } else {
                        // 不是标签，'[' 当作普通字符
                        ctx.currentText.append('[');
                        ctx.currentText.append(c);
                        ctx.state = ParserState.TEXT;
                    }
                }
                case TAG_NAME -> {
                    // 正在读取标签名称
                    if (c == '=') {
                        // 标签有参数
                        ctx.pendingTagName = ctx.tagBuffer.toString();
                        ctx.tagBuffer.setLength(0);
                        ctx.state = ParserState.TAG_PARAM;
                    } else if (c == ']') {
                        // 无参数标签完成
                        ctx.pendingTagName = ctx.tagBuffer.toString();
                        boolean result = ctx.applyOpenTag(ctx.pendingTagName, null);
                        if (!result) {
                            ctx.currentText.append('[');
                            ctx.currentText.append(ctx.pendingTagName);
                            ctx.currentText.append(']');
                        }
                        ctx.state = ParserState.TEXT;
                    } else if (Character.isLetterOrDigit(c)) {
                        ctx.tagBuffer.append(c);
                    } else {
                        // 无效标签，丢弃
                        ctx.currentText.append('[');
                        ctx.currentText.append(ctx.tagBuffer);
                        ctx.currentText.append(c);
                        ctx.tagBuffer.setLength(0);
                        ctx.state = ParserState.TEXT;
                    }
                }
                case TAG_PARAM -> {
                    // 正在读取标签参数
                    if (c == ']') {
                        // 有参数标签完成
                        ctx.pendingTagParam = ctx.tagBuffer.toString();
                        boolean result = ctx.applyOpenTag(ctx.pendingTagName, ctx.pendingTagParam);
                        if (!result) {
                            ctx.currentText.append('[');
                            ctx.currentText.append(ctx.pendingTagName);
                            ctx.currentText.append(']');
                        }
                        ctx.state = ParserState.TEXT;
                    } else {
                        ctx.tagBuffer.append(c);
                    }
                }
                case CLOSE_TAG -> {
                    // 遇到 '[/'，等待标签名称
                    if (Character.isLetter(c)) {
                        ctx.state = ParserState.CLOSE_NAME;
                        ctx.tagBuffer.setLength(0);
                        ctx.tagBuffer.append(c);
                    } else {
                        // 无效的 '[/'，当作普通字符
                        ctx.currentText.append('[');
                        ctx.currentText.append('/');
                        ctx.currentText.append(c);
                        ctx.state = ParserState.TEXT;
                    }
                }
                case CLOSE_NAME -> {
                    // 正在读取关闭标签名称
                    if (c == ']') {
                        // 关闭标签完成
                        String closeTagName = ctx.tagBuffer.toString();
                        // 检查是否是有效的关闭标签
                        if (isValidTagName(closeTagName)) {
                            ctx.applyCloseTag(closeTagName);
                        } else {
                            // 无效标签，当作普通文本
                            ctx.currentText.append("[/");
                            ctx.currentText.append(closeTagName);
                            ctx.currentText.append("]");
                        }
                        ctx.state = ParserState.TEXT;
                    } else if (Character.isLetterOrDigit(c)) {
                        ctx.tagBuffer.append(c);
                    } else {
                        // 无效关闭标签 - 将已读取的内容当作普通文本
                        ctx.currentText.append("[/");
                        ctx.currentText.append(ctx.tagBuffer.toString());
                        ctx.currentText.append(c);
                        ctx.tagBuffer.setLength(0);
                        ctx.state = ParserState.TEXT;
                    }
                }
            }
        }

        // 处理结束时的状态
        if (ctx.state == ParserState.TEXT) {
            ctx.flushText();
        }

        // 添加最后一行（如果有内容）
        if (!ctx.currentLine.segments.isEmpty()) {
            ctx.currentLine.mergeSegments();
            ctx.parsedLines.add(ctx.currentLine);
        }
    }

    /**
     * Check if a tag name is valid (supported by the parser).
     */
    private static boolean isValidTagName(String tagName) {
        return switch (tagName.toLowerCase()) {
            case "color", "c", "bg", "background",
                 "size", "s", "align", "a", "lineheight", "lh",
                 "bold", "b", "underline", "u", "italic", "i" -> true;
            default -> false;
        };
    }

    /**
     * Parse color from various formats:
     * - Hex ARGB: #AARRGGBB or #ARGB
     * - Hex RGB: #RRGGBB or #RGB
     * - Named colors: red, green, blue, etc.
     */
    private static Integer parseColor(String colorStr) {
        colorStr = colorStr.trim().toLowerCase();

        // Handle hex color
        if (colorStr.startsWith("#")) {
            try {
                String hex = colorStr.substring(1);

                // 8位格式: #AARRGGBB
                if (hex.length() == 8) {
                    return (Integer.parseInt(hex, 16));
                }
                // 4位格式: #ARGB
                else if (hex.length() == 4) {
                    // Expand #ARGB to #AARRGGBB
                    int a = Integer.parseInt(hex.substring(0, 1), 16) * 17;
                    int r = Integer.parseInt(hex.substring(1, 2), 16) * 17;
                    int g = Integer.parseInt(hex.substring(2, 3), 16) * 17;
                    int b = Integer.parseInt(hex.substring(3, 4), 16) * 17;
                    return (a << 24) | (r << 16) | (g << 8) | b;
                }
                // 6位格式: #RRGGBB
                else if (hex.length() == 6) {
                    int rgb = Integer.parseInt(hex, 16);
                    return 0xFF000000 | rgb; // Add full alpha
                }
                // 3位格式: #RGB
                else if (hex.length() == 3) {
                    // Expand #RGB to #RRGGBB
                    int r = Integer.parseInt(hex.substring(0, 1), 16) * 17;
                    int g = Integer.parseInt(hex.substring(1, 2), 16) * 17;
                    int b = Integer.parseInt(hex.substring(2, 3), 16) * 17;
                    return 0xFF000000 | (r << 16) | (g << 8) | b;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Handle named colors
        return switch (colorStr) {
            case "black" -> 0xFF000000;
            case "dark_blue" -> 0xFF0000AA;
            case "dark_green" -> 0xFF00AA00;
            case "dark_aqua" -> 0xFF00AAAA;
            case "dark_red" -> 0xFFAA0000;
            case "dark_purple" -> 0xFFAA00AA;
            case "gold" -> 0xFFFFAA00;
            case "gray" -> 0xFFAAAAAA;
            case "dark_gray" -> 0xFF555555;
            case "blue" -> 0xFF5555FF;
            case "green" -> 0xFF55FF55;
            case "aqua" -> 0xFF55FFFF;
            case "red" -> 0xFFFF5555;
            case "light_purple" -> 0xFFFF55FF;
            case "yellow" -> 0xFFFFFF55;
            case "white" -> 0xFFFFFFFF;
            default -> null;
        };
    }
}
