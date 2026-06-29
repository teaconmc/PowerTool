package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class TextField extends AbstractWidget {
    private final Font font;
    private String value = "";
    private int maxLength = 32;
    private boolean canLoseFocus = true;
    private boolean isEditable = true;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor = 0xffffffff;
    private int textColorUneditable = 7368816;
    private final int innerX = 2;

    @Nullable
    private Consumer<String> responder;
    private Predicate<String> filter = Objects::nonNull;
    private Predicate<String> confirm = Objects::nonNull;
    private final BiFunction<String, Integer, FormattedCharSequence> formatter = (p_94147_, p_94148_) -> FormattedCharSequence.forward(p_94147_, Style.EMPTY);
    private long focusedTime = Util.getMillis();
    private boolean textShadow;
    private boolean error;

    private int autoConfirm = -1;

    public TextField(Font font, int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.font = font;
    }

    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }

    public void setConfirm(Predicate<String> confirm) {
        this.confirm = confirm;
    }

    @NotNull
    @Override
    protected MutableComponent createNarrationMessage() {
        Component component = this.getMessage();
        return Component.translatable("gui.narrate.editBox", component, this.value);
    }

    public void setValue(String text) {
        if (this.filter.test(text)) {
            if (text.length() > this.maxLength) {
                this.value = text.substring(0, this.maxLength);
            } else {
                this.value = text;
            }

            this.moveCursorToEnd(false);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(text);
        }
    }

    public String getHighlighted() {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(i, j);
    }

    public void insertText(String textToWrite) {
        this.autoConfirm = 60;
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        int k = this.maxLength - this.value.length() - (i - j);
        if (k > 0) {
            String s = StringUtil.filterText(textToWrite);
            int l = s.length();
            if (k < l) {
                if (Character.isHighSurrogate(s.charAt(k - 1))) {
                    k--;
                }

                s = s.substring(0, k);
                l = k;
            }

            String s1 = new StringBuilder(this.value).replace(i, j, s).toString();
            if (this.filter.test(s1)) {
                this.value = s1;
                this.setCursorPosition(i + l);
                this.setHighlightPos(this.cursorPos);
                this.onValueChange(this.value);
            }
        }
    }

    protected void onValueChange(String newText) {
        if (this.responder != null) {
            this.responder.accept(newText);
        }
    }

    private void deleteText(int count, boolean ctrl) {
        this.autoConfirm = 60;
        if (ctrl) {
            this.deleteWords(count);
        } else {
            this.deleteChars(count);
        }
    }

    /**
     * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in which case the selection is deleted instead.
     */
    public void deleteWords(int num) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteCharsToPos(this.getWordPosition(num));
            }
        }
    }

    public void deleteChars(int num) {
        this.deleteCharsToPos(this.getCursorPos(num));
    }

    public void deleteCharsToPos(int num) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = Math.min(num, this.cursorPos);
                int j = Math.max(num, this.cursorPos);
                if (i != j) {
                    String s = new StringBuilder(this.value).delete(i, j).toString();
                    if (this.filter.test(s)) {
                        this.value = s;
                        this.moveCursorTo(i, false);
                    }
                }
            }
        }
    }

    public int getWordPosition(int numWords) {
        return this.getWordPosition(numWords, this.getCursorPosition());
    }

    private int getWordPosition(int numWords, int pos) {
        int i = pos;
        boolean flag = numWords < 0;
        int j = Math.abs(numWords);

        for (int k = 0; k < j; k++) {
            if (!flag) {
                int l = this.value.length();
                i = this.value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (i < l && this.value.charAt(i) == ' ') {
                        i++;
                    }
                }
            } else {
                while (i > 0 && this.value.charAt(i - 1) == ' ') {
                    i--;
                }

                while (i > 0 && this.value.charAt(i - 1) != ' ') {
                    i--;
                }
            }
        }

        return i;
    }

    public void moveCursor(int delta, boolean select) {
        this.moveCursorTo(this.getCursorPos(delta), select);
    }

    private int getCursorPos(int delta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, delta);
    }

    public void moveCursorTo(int delta, boolean select) {
        this.setCursorPosition(delta);
        if (!select) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    public void setCursorPosition(int pos) {
        this.cursorPos = Mth.clamp(pos, 0, this.value.length());
        this.scrollTo(this.cursorPos);
    }

    public void moveCursorToStart(boolean select) {
        this.moveCursorTo(0, select);
    }

    public void moveCursorToEnd(boolean select) {
        this.moveCursorTo(this.value.length(), select);
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {

        var keyCode = event.key();
        var ctrl    = event.hasControlDown();
        var shift   = event.hasShiftDown();
        var alt     = event.hasAltDown();

        if (this.isActive() && this.isFocused()) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE:
                    if (this.isEditable) {
                        this.deleteText(-1, ctrl);
                    }

                    return true;
                case GLFW.GLFW_KEY_DELETE:
                    if (this.isEditable) {
                        this.deleteText(1, ctrl);
                    }

                    return true;
                case GLFW.GLFW_KEY_RIGHT:
                    if (ctrl) {
                        this.moveCursorTo(this.getWordPosition(1), shift);
                    } else {
                        this.moveCursor(1, shift);
                    }

                    return true;
                case GLFW.GLFW_KEY_LEFT:
                    if (ctrl) {
                        this.moveCursorTo(this.getWordPosition(-1), shift);
                    } else {
                        this.moveCursor(-1, shift);
                    }

                    return true;
                case GLFW.GLFW_KEY_HOME:
                    this.moveCursorToStart(shift);
                    return true;
                case GLFW.GLFW_KEY_END:
                    this.moveCursorToEnd(shift);
                    return true;
                case GLFW.GLFW_KEY_ENTER:
                    this.onConfirm();
                    return true;
                case GLFW.GLFW_KEY_INSERT:
                case GLFW.GLFW_KEY_DOWN:
                case GLFW.GLFW_KEY_UP:
                case GLFW.GLFW_KEY_PAGE_UP:
                case GLFW.GLFW_KEY_PAGE_DOWN:
                default:
                    if (event.isSelectAll()) {
                        this.moveCursorToEnd(false);
                        this.setHighlightPos(0);
                        return true;
                    } else if (event.isCopy()) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                        return true;
                    } else if (event.isPaste()) {
                        if (this.isEditable()) {
                            this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                        }

                        return true;
                    } else {
                        if (event.isCut()) {
                            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                            if (this.isEditable()) {
                                this.insertText("");
                            }

                            return true;
                        }

                        return false;
                    }
            }
        } else {
            return false;
        }
    }

    public boolean canConsumeInput() {
        return this.isActive() && this.isFocused() && this.isEditable();
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (StringUtil.isAllowedChatCharacter(event.codepoint())) {
            if (this.isEditable) {
                this.insertText(Character.toString(event.codepoint()));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(final MouseButtonEvent event, final boolean doubleClick) {
        
        var mouseX  = event.x();
        var shift   = event.hasShiftDown();

        int i = Mth.floor(mouseX) - this.getX() - this.innerX;
        String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
        this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + this.displayPos, shift);
        
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void extractWidgetRenderState(
            final GuiGraphicsExtractor  graphics,
            final int                   mouseX,
            final int                   mouseY,
            final float                 partialTick
    ) {
        if (this.autoConfirm > 0) {
            this.autoConfirm --;
        } else if (this.autoConfirm == 0) {
            this.onConfirm();
            this.autoConfirm = -1;
        }

        if (this.isVisible()) {

            int textColor = this.isEditable ? this.textColor : this.textColorUneditable;
            int i = this.cursorPos - this.displayPos;
            String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean flag = i >= 0 && i <= string.length();
            boolean flag1 = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L && flag;
            int left = this.getX() + this.innerX;
            int top = this.getY() + (this.height - 8) / 2;
            int l = left;
            int i1 = Mth.clamp(this.highlightPos - this.displayPos, 0, string.length());
            if (!string.isEmpty()) {
                String s1 = flag ? string.substring(0, i) : string;
                final var text = this.formatter.apply(s1, this.displayPos);
                final var width1 = this.font.width(text);
                graphics.text(this.font, text, left, top, textColor, this.textShadow);
                l = left + width1;
            }

            boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int j1 = l;
            if (!flag) {
                j1 = i > 0 ? left + this.width : left;
            } else if (flag2) {
                j1 = l - 1;
                l--;
            }

            if (!string.isEmpty() && flag && i < string.length()) {
                graphics.text(this.font, this.formatter.apply(string.substring(i), this.cursorPos), l, top, textColor, this.textShadow);
            }

            if (flag1) {
                if (flag2) {
                    graphics.fill(j1, top - 1, j1 + 1, top + 1 + 9, -3092272);
                } else {
                    graphics.text(this.font, "_", j1, top, textColor, this.textShadow);
                }
            }

            if (i1 != i) {
                int k1 = left + this.font.width(string.substring(0, i1));
                this.renderHighlight(graphics, j1, top - 1, k1 - 1, top + 1 + 9);
            }

            if (this.error) {
                graphics.centeredText(Minecraft.getInstance().font, Component.literal("ERROR"), this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() + 4, 0xffaa0000);
            }
        }
    }

    private void renderHighlight(
            GuiGraphicsExtractor graphics,
            int minX,
            int minY,
            int maxX,
            int maxY
    ) {
        if (minX < maxX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            int j = minY;
            minY = maxY;
            maxY = j;
        }

        if (maxX > this.getX() + this.width) {
            maxX = this.getX() + this.width;
        }

        if (minX > this.getX() + this.width) {
            minX = this.getX() + this.width;
        }

        graphics.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, minX, minY, maxX, maxY, -16776961);
    }

    public void setMaxLength(int length) {
        this.maxLength = length;
        if (this.value.length() > length) {
            this.value = this.value.substring(0, length);
            this.onValueChange(this.value);
        }
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    @Override
    public void setFocused(boolean focused) {
        if (this.canLoseFocus || focused) {
            super.setFocused(focused);
            if (focused) {
                this.focusedTime = Util.getMillis();
            } else {
                this.onConfirm();
            }
        }
    }

    private void onConfirm() {
        if (this.confirm != null) {
            this.error = !this.confirm.test(this.getValue());
        }
    }

    private boolean isEditable() {
        return this.isEditable;
    }

    public int getInnerWidth() {
        return this.width - this.innerX * 2;
    }

    public void setHighlightPos(int position) {
        this.highlightPos = Mth.clamp(position, 0, this.value.length());
        this.scrollTo(this.highlightPos);
    }

    private void scrollTo(int position) {
        if (this.font != null) {
            this.displayPos = Math.min(this.displayPos, this.value.length());
            int i = this.getInnerWidth();
            String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), i);
            int j = s.length() + this.displayPos;
            if (position == this.displayPos) {
                this.displayPos = this.displayPos - this.font.plainSubstrByWidth(this.value, i, true).length();
            }

            if (position > j) {
                this.displayPos += position - j;
            } else if (position <= this.displayPos) {
                this.displayPos = this.displayPos - (this.displayPos - position);
            }

            this.displayPos = Mth.clamp(this.displayPos, 0, this.value.length());
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean isVisible) {
        this.visible = isVisible;
    }

    public String getValue() {
        return value;
    }

    public void setCanLoseFocus(final boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
    }

    public void setEditable(final boolean editable) {
        this.isEditable = editable;
    }

    public void setTextColor(final int textColor) {
        this.textColor = textColor;
    }

    public void setTextColorUneditable(final int textColorUneditable) {
        this.textColorUneditable = textColorUneditable;
    }

    public void setFilter(final Predicate<String> filter) {
        this.filter = filter;
    }

    public void setTextShadow(final boolean textShadow) {
        this.textShadow = textShadow;
    }

    public int getScreenX(int charNum) {
        return charNum > this.value.length() ? this.getX() : this.getX() + this.font.width(this.value.substring(0, charNum));
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }

    public boolean getTextShadow() {
        return this.textShadow;
    }
}
