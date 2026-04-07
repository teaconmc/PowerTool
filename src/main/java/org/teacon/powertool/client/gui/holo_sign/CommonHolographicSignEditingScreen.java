/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.client.gui.holo_sign;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;

import java.util.Arrays;
import java.util.Objects;

@NonNullByDefault
public class CommonHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<CommonHolographicSignBlockEntity> {
    
    public static final int MAXIMUM_LINE_COUNT = 10;
    
    private int frame;
    private int line;
    private TextFieldHelper signField;
    private final String[] messages;
    
    public CommonHolographicSignEditingScreen(CommonHolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit"), theSign);
        var size = theSign.contents.size();
        this.messages = new String[Math.max(size, MAXIMUM_LINE_COUNT)];
        Arrays.fill(this.messages, "");
        for (int i = 0; i < size; i++) {
            this.messages[i] = theSign.contents.get(i).getString();
        }
    }
    
    @Override
    protected void init() {
        super.init();
        var mc = Objects.requireNonNull(this.minecraft, "Minecraft instance is missing while Screen is initializing!");
        this.signField = new TextFieldHelper(
                () -> this.messages[this.line],
                (str) -> this.messages[this.line] = str,
                TextFieldHelper.createClipboardGetter(mc),
                TextFieldHelper.createClipboardSetter(mc),
                str -> true
        );
    }
    
    @Override
    protected void writeBackToBE() {
        super.writeBackToBE();
        int last = this.messages.length - 1;
        for (; last >= 0; last--) {
            if (this.messages[last] != null && !this.messages[last].isEmpty()) {
                break;
            }
        }
        var toSend = Arrays.copyOfRange(this.messages, 0, last + 1);
        this.sign.contents = Arrays.stream(toSend).map(Component::literal).limit(MAXIMUM_LINE_COUNT).toList();
    }
    
    @Override
    public void tick() {
        ++this.frame;
        super.tick();
    }
    
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.colorInput.charTyped(event)) return true;
        if (this.yRotationInput.charTyped(event)) return true;
        if (this.xRotationInput.charTyped(event)) return true;
        if (this.zOffsetInput.charTyped(event)) return true;
        if (!colorInput.isFocused() && !yRotationInput.isFocused() && this.signField.charTyped(event)) return true;
        return super.charTyped(event);
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        var keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_UP) {
            // Move up one line
            this.line = (this.line - 1) % this.messages.length;
            if (this.line < 0) {
                this.line = 0;
            }
            this.signField.setCursorToEnd();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            // Move down one line
            this.line = (this.line + 1) % this.messages.length;
            if (this.line >= this.messages.length) {
                this.line = this.messages.length - 1;
            }
            this.signField.setCursorToEnd();
            return true;
        } else {
            // Regular typing
            return (!colorInput.isFocused() && !yRotationInput.isFocused() && !this.xRotationInput.isFocused() && !this.zOffsetInput.isFocused() && this.signField.keyPressed(event)) || super.keyPressed(event);
        }
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        // I don't know, someone please explain why these transforms are necessary???
        var poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(0.0F, 45.0F);
        poseStack.scale(93.75F, -93.75F);
        poseStack.translate(0.0F, -1.625F);
        // Render the text and cursor
        boolean showCursor = this.frame / 6 % 2 == 0;
        poseStack.translate(0, 1.0F / 3.0F);
        poseStack.scale(1F / 96F, -1F / 96F);
        int cursorPos = this.signField.getCursorPos();
        int selectionPos = this.signField.getSelectionPos();
        int cursorY = this.line * 10 - this.messages.length * 5;
        for (int line = 0; line < this.messages.length; ++line) {
            String text = this.messages[line];
            if (text != null) {
                if (this.font.isBidirectional()) {
                    text = this.font.bidirectionalShaping(text);
                }
                int xStart = this.getLineXStart(text);
                graphics.text(this.font, text, xStart, line * 10 - this.messages.length * 5, 0xFFFFFF, false);
                if (line == this.line && cursorPos >= 0 && showCursor) {
                    int j1 = this.font.width(text.substring(0, Math.min(cursorPos, text.length())));
                    int cursorX = (int) switch (this.textAlign) {
                        case LEFT -> this.width / 10.0 + j1;
                        case CENTER -> this.width / 2.0 + j1 - this.font.width(text) / 2.0;
                        case RIGHT -> this.width * 0.9F;
                    };
                    if (cursorPos >= text.length() && (!this.colorInput.isFocused() && !this.yRotationInput.isFocused())) {
                        graphics.text(this.font, "_", cursorX, cursorY, 0xFFFFFF, false);
                    }
                }
            }
        }
        
        // Render selection highlights
        for (int i = 0; i < this.messages.length; ++i) {
            String text = this.messages[i];
            if (text != null && i == this.line && cursorPos >= 0) {
                int xStart = this.getLineXStart(text);
                int j3 = this.font.width(text.substring(0, Math.min(cursorPos, text.length())));
                int k3 = j3 + xStart - 1;
                if (showCursor && cursorPos < text.length()) {
                    graphics.fill(k3, cursorY - 1, k3 + 1, cursorY + 9, 0xFFFFFFFF);
                }
                
                if (selectionPos != cursorPos) {
                    int l3 = Math.min(cursorPos, selectionPos);
                    int l1 = Math.max(cursorPos, selectionPos);
                    int i2 = this.font.width(text.substring(0, l3)) - this.font.width(text) / 2;
                    int j2 = this.font.width(text.substring(0, l1)) - this.font.width(text) / 2;
                    int k2 = Math.min(i2, j2);
                    int l2 = Math.max(i2, j2);
                    graphics.fill(k2, cursorY, l2, cursorY + 10, -16776961);
                }
            }
        }
        
        poseStack.popMatrix();
    }
    
    protected int getLineXStart(String text) {
        return (int) switch (this.textAlign) {
            case LEFT -> this.width / 10.0;
            case CENTER -> this.width / 2.0 - this.font.width(text) / 2.0;
            case RIGHT -> this.width * 0.9 - this.font.width(text);
        };
    }
}
