package org.teacon.powertool.client.gui.widget;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dev.vfyjxf.taffy.style.TextAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.IMEPreeditOverlay;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.TextCursorUtils;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;

import java.util.function.Consumer;

@NonNullByDefault
public class MultiLineEditBox extends AbstractTextAreaWidget {
    
    private static final int CURSOR_COLOR = -3092272;
    private static final int PLACEHOLDER_TEXT_COLOR = ARGB.color(204, -2039584);
    private static final int UNWRAPPED_TEXT_FIELD_WIDTH = Integer.MAX_VALUE / 4;
    private static final int TEXT_HEIGHT = 9;
    private static final int LINE_HEIGHT = 12;
    private static final int LINE_MARKER_OFFSET = 9;
    private static final int LINE_COLOR = 0x66FFFFFF;
    
    private final Font font;
    private final Component placeholder;
    private final MultilineTextField textField;
    private final int textColor;
    private final boolean textShadow;
    private final int cursorColor;
    private int maxLines = Integer.MAX_VALUE;
    private int maxLineLength = Integer.MAX_VALUE;
    private TextAlign textAlign = TextAlign.CENTER;
    private @Nullable IMEPreeditOverlay preeditOverlay;
    private long focusedTime = Util.getMillis();
    
    public MultiLineEditBox(Font font, int x, int y, int width, int height, Component placeholder, Component narration, int textColor, boolean textShadow, int cursorColor, boolean showBackground, boolean showDecorations) {
        super(x, y, width, height, narration, AbstractScrollArea.defaultSettings((int) (TEXT_HEIGHT / 2.0)), showBackground, showDecorations);
        this.font = font;
        this.placeholder = placeholder;
        this.textColor = textColor;
        this.textShadow = textShadow;
        this.cursorColor = cursorColor;
        this.textField = new MultilineTextField(font, UNWRAPPED_TEXT_FIELD_WIDTH);
        this.textField.setCursorListener(this::scrollToCursor);
    }
    
    public MultiLineEditBox() {
        this(Minecraft.getInstance().font, 0, 0, 0, 0, CommonComponents.EMPTY, CommonComponents.EMPTY, -2039584, true, CURSOR_COLOR, true, true);
    }
    
    public void setMaxLines(int maxLines) {
        this.maxLines = Math.max(1, maxLines);
        this.textField.setLineLimit(this.maxLines);
        this.setValue(this.getValue());
    }
    
    public void setMaxLineLength(int maxLineLength) {
        this.maxLineLength = Math.max(0, maxLineLength);
        this.setValue(this.getValue());
    }
    
    public void setTextAlign(TextAlign textAlign) {
        this.textAlign = textAlign;
    }
    
    public TextAlign getTextAlign() {
        if (this.textAlign == TextAlign.LEFT) {
            return TextAlign.LEFT;
        }
        if (this.textAlign == TextAlign.RIGHT) {
            return TextAlign.RIGHT;
        }
        return TextAlign.CENTER;
    }
    
    public void setValueListener(Consumer<String> valueListener) {
        this.textField.setValueListener(valueListener);
    }
    
    public void setValue(String value) {
        this.textField.setValue(this.sanitizeValue(value), true);
    }
    
    public String getValue() {
        return this.textField.value();
    }
    
    public int getLineCount() {
        return this.textField.getLineCount();
    }
    
    public int getHorizontalContentWidth() {
        int result = 0;
        String value = this.textField.value();
        for (MultilineTextField.StringView lineView : this.textField.iterateLines()) {
            result = Math.max(result, this.font.width(value.substring(lineView.beginIndex(), lineView.endIndex())));
        }
        return result;
    }
    
    public int getMeasuredWidth() {
        return this.getHorizontalContentWidth() + this.totalInnerPadding();
    }
    
    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
    }
    
    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (doubleClick) {
            this.textField.selectWordAtCursor();
        } else {
            this.textField.setSelecting(event.hasShiftDown());
            this.seekCursorScreen(event.x(), event.y());
        }
    }
    
    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        this.textField.setSelecting(true);
        this.seekCursorScreen(event.x(), event.y());
        this.textField.setSelecting(event.hasShiftDown());
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        this.textField.setSelecting(event.hasShiftDown());
        if (event.isSelectAll()) {
            this.textField.seekCursor(Whence.ABSOLUTE, 0);
            this.textField.setSelecting(true);
            this.textField.seekCursor(Whence.END, 0);
            this.textField.setSelecting(event.hasShiftDown());
            return true;
        }
        if (event.isCopy()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.textField.getSelectedText());
            return true;
        }
        if (event.isPaste()) {
            this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        }
        if (event.isCut()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.textField.getSelectedText());
            this.insertText("");
            return true;
        }
        return switch (event.key()) {
            case 257, 335 -> {
                this.insertText("\n");
                yield true;
            }
            case 259 -> {
                if (event.hasControlDownWithQuirk()) {
                    MultilineTextField.StringView wordView = this.textField.getPreviousWord();
                    this.textField.deleteText(wordView.beginIndex() - this.textField.cursor());
                } else {
                    this.textField.deleteText(-1);
                }
                yield true;
            }
            case 261 -> {
                if (event.hasControlDownWithQuirk()) {
                    MultilineTextField.StringView wordView = this.textField.getNextWord();
                    this.textField.deleteText(wordView.beginIndex() - this.textField.cursor());
                } else {
                    this.textField.deleteText(1);
                }
                yield true;
            }
            case 262 -> {
                if (event.hasControlDownWithQuirk()) {
                    MultilineTextField.StringView wordView = this.textField.getNextWord();
                    this.textField.seekCursor(Whence.ABSOLUTE, wordView.beginIndex());
                } else {
                    this.textField.seekCursor(Whence.RELATIVE, 1);
                }
                yield true;
            }
            case 263 -> {
                if (event.hasControlDownWithQuirk()) {
                    MultilineTextField.StringView wordView = this.textField.getPreviousWord();
                    this.textField.seekCursor(Whence.ABSOLUTE, wordView.beginIndex());
                } else {
                    this.textField.seekCursor(Whence.RELATIVE, -1);
                }
                yield true;
            }
            case 264 -> {
                if (!event.hasControlDownWithQuirk()) {
                    this.textField.seekCursorLine(1);
                }
                yield true;
            }
            case 265 -> {
                if (!event.hasControlDownWithQuirk()) {
                    this.textField.seekCursorLine(-1);
                }
                yield true;
            }
            case 266 -> {
                this.textField.seekCursor(Whence.ABSOLUTE, 0);
                yield true;
            }
            case 267 -> {
                this.textField.seekCursor(Whence.END, 0);
                yield true;
            }
            case 268 -> {
                if (event.hasControlDownWithQuirk()) {
                    this.textField.seekCursor(Whence.ABSOLUTE, 0);
                } else {
                    this.textField.seekCursor(Whence.ABSOLUTE, this.textField.getLineView(this.textField.getLineAtCursor()).beginIndex());
                }
                yield true;
            }
            case 269 -> {
                if (event.hasControlDownWithQuirk()) {
                    this.textField.seekCursor(Whence.END, 0);
                } else {
                    this.textField.seekCursor(Whence.ABSOLUTE, this.textField.getLineView(this.textField.getLineAtCursor()).endIndex());
                }
                yield true;
            }
            default -> false;
        };
    }
    
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.visible && this.isFocused() && event.isAllowedChatCharacter()) {
            this.insertText(event.codepointAsString());
            return true;
        }
        return false;
    }
    
    @Override
    public boolean preeditUpdated(@Nullable PreeditEvent event) {
        this.preeditOverlay = event != null ? new IMEPreeditOverlay(event, this.font, TEXT_HEIGHT + 1) : null;
        return true;
    }
    
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        String value = this.textField.value();
        if (value.isEmpty() && !this.isFocused()) {
            int drawTop = this.getContentTop();
            graphics.textWithWordWrap(this.font, this.placeholder, this.getInnerLeft(), drawTop, this.width - this.totalInnerPadding(), PLACEHOLDER_TEXT_COLOR);
            this.extractLineMarkers(graphics, drawTop);
            return;
        }
        int cursor = this.textField.cursor();
        boolean showCursor = this.isFocused() && TextCursorUtils.isCursorVisible(Util.getMillis() - this.focusedTime);
        boolean needsValidCursorPos = this.preeditOverlay != null;
        boolean insertCursor = cursor < value.length();
        int cursorX = 0;
        int cursorY = 0;
        int drawTop = this.getContentTop();
        boolean hasDrawnCursor = false;
        
        for (MultilineTextField.StringView lineView : this.textField.iterateLines()) {
            int lineWidth = this.lineWidth(value, lineView);
            int textLeft = this.getInnerLeft() + this.getAlignOffset(lineWidth);
            boolean lineWithinVisibleBounds = this.withinContentAreaTopBottom(drawTop, drawTop + LINE_HEIGHT);
            if (!hasDrawnCursor && (needsValidCursorPos || showCursor) && insertCursor && cursor >= lineView.beginIndex() && cursor <= lineView.endIndex()) {
                if (lineWithinVisibleBounds) {
                    String textBeforeCursor = value.substring(lineView.beginIndex(), cursor);
                    String textAfterCursor = value.substring(cursor, lineView.endIndex());
                    int textBeforeCursorPosRight = textLeft + this.font.width(textBeforeCursor);
                    graphics.text(this.font, textBeforeCursor, textLeft, drawTop, this.textColor, this.textShadow);
                    graphics.text(this.font, textAfterCursor, textBeforeCursorPosRight, drawTop, this.textColor, this.textShadow);
                    cursorX = textBeforeCursorPosRight;
                    cursorY = drawTop;
                    if (showCursor) {
                        TextCursorUtils.extractInsertCursor(graphics, textBeforeCursorPosRight, drawTop, this.cursorColor, TEXT_HEIGHT + 1);
                    }
                    hasDrawnCursor = true;
                }
            } else if (lineWithinVisibleBounds) {
                String substring = value.substring(lineView.beginIndex(), lineView.endIndex());
                graphics.text(this.font, substring, textLeft, drawTop, this.textColor, this.textShadow);
                if ((needsValidCursorPos || showCursor) && !insertCursor) {
                    cursorX = textLeft + this.font.width(substring);
                    cursorY = drawTop;
                }
            }
            drawTop += LINE_HEIGHT;
        }
        
        if (showCursor && !insertCursor && this.withinContentAreaTopBottom(cursorY, cursorY + LINE_HEIGHT)) {
            TextCursorUtils.extractAppendCursor(graphics, this.font, cursorX, cursorY, this.cursorColor, this.textShadow);
        }
        this.extractSelection(graphics, value);
        this.extractLineMarkers(graphics, this.getContentTop());
        if (this.isHovered()) {
            graphics.requestCursor(CursorTypes.IBEAM);
        }
        if (this.preeditOverlay != null) {
            this.preeditOverlay.updateInputPosition(cursorX, cursorY);
            graphics.setPreeditOverlay(this.preeditOverlay);
        }
    }
    
    @Override
    protected int getInnerHeight() {
        return LINE_HEIGHT * this.textField.getLineCount();
    }
    
    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            this.focusedTime = Util.getMillis();
        }
        Minecraft.getInstance().onTextInputFocusChange(this, focused);
    }
    
    private void insertText(String input) {
        String oldValue = this.textField.value();
        this.textField.insertText(input);
        String newValue = this.textField.value();
        String sanitized = this.sanitizeValue(newValue);
        if (!sanitized.equals(newValue)) {
            if (oldValue.equals(sanitized)) {
                this.textField.setValue(oldValue, true);
            } else {
                this.textField.setValue(sanitized, true);
            }
        }
    }
    
    private String sanitizeValue(String value) {
        String normalized = StringUtil.filterText(value.replace("\r\n", "\n").replace('\r', '\n'), true);
        String[] lines = normalized.split("\n", -1);
        int count = Math.min(lines.length, this.maxLines);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                result.append('\n');
            }
            result.append(this.truncateLine(lines[i]));
        }
        return result.toString();
    }
    
    private String truncateLine(String line) {
        if (line.length() <= this.maxLineLength) {
            return line;
        }
        return line.substring(0, this.maxLineLength);
    }
    
    private void extractSelection(GuiGraphicsExtractor graphics, String value) {
        if (!this.textField.hasSelection()) {
            return;
        }
        MultilineTextField.StringView selection = this.textField.getSelected();
        int drawTop = this.getContentTop();
        for (MultilineTextField.StringView lineView : this.textField.iterateLines()) {
            if (selection.beginIndex() > lineView.endIndex()) {
                drawTop += LINE_HEIGHT;
                continue;
            }
            if (lineView.beginIndex() > selection.endIndex()) {
                break;
            }
            if (this.withinContentAreaTopBottom(drawTop, drawTop + LINE_HEIGHT)) {
                int lineWidth = this.lineWidth(value, lineView);
                int textLeft = this.getInnerLeft() + this.getAlignOffset(lineWidth);
                int contentLeft = this.getInnerLeft();
                int contentRight = this.getX() + this.width - this.innerPadding();
                int drawBegin;
                if (selection.beginIndex() <= lineView.beginIndex()) {
                    drawBegin = contentLeft;
                } else {
                    drawBegin = textLeft + this.font.width(value.substring(lineView.beginIndex(), selection.beginIndex()));
                }
                int drawEnd;
                if (selection.endIndex() > lineView.endIndex()) {
                    drawEnd = contentRight;
                } else {
                    drawEnd = textLeft + this.font.width(value.substring(lineView.beginIndex(), selection.endIndex()));
                }
                graphics.textHighlight(drawBegin, drawTop, drawEnd, drawTop + TEXT_HEIGHT, true);
            }
            drawTop += LINE_HEIGHT;
        }
    }
    
    private void extractLineMarkers(GuiGraphicsExtractor graphics, int firstLineTop) {
        int drawTop = firstLineTop;
        int left = this.getInnerLeft();
        int right = this.getX() + this.width - this.innerPadding();
        for (int i = 0; i < this.textField.getLineCount(); i++) {
            int lineY = drawTop + LINE_MARKER_OFFSET;
            if (this.withinContentAreaTopBottom(drawTop, drawTop + LINE_HEIGHT)) {
                graphics.fill(left, lineY, right, lineY + 1, LINE_COLOR);
            }
            drawTop += LINE_HEIGHT;
        }
    }
    
    private int lineWidth(String value, MultilineTextField.StringView lineView) {
        return this.font.width(value.substring(lineView.beginIndex(), lineView.endIndex()));
    }
    
    private int getAlignOffset(int lineWidth) {
        int contentWidth = this.width - this.totalInnerPadding();
        return switch (this.getTextAlign()) {
            case LEFT -> 0;
            case RIGHT -> contentWidth - lineWidth;
            default -> (contentWidth - lineWidth) / 2;
        };
    }
    
    private int getContentTop() {
        int freeHeight = this.height - this.totalInnerPadding() - this.getInnerHeight();
        return this.getInnerTop() + Math.max(0, freeHeight / 2);
    }
    
    private void scrollToCursor() {
        this.setScrollAmount(0);
    }
    
    private void seekCursorScreen(double x, double y) {
        double mouseY = y - this.getContentTop() + this.scrollAmount();
        int lineIndex = Mth.clamp(Mth.floor(mouseY / LINE_HEIGHT), 0, this.textField.getLineCount() - 1);
        MultilineTextField.StringView lineView = this.textField.getLineView(lineIndex);
        int lineWidth = this.lineWidth(this.textField.value(), lineView);
        double mouseX = x - this.getInnerLeft() - this.getAlignOffset(lineWidth);
        this.textField.seekCursorToPoint(mouseX, lineIndex * LINE_HEIGHT);
    }
}
