/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.client.gui.holo_sign;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;

import java.util.Arrays;
import java.util.Objects;

public class CommonHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<CommonHolographicSignBlockEntity> {
    
    public static final int MAXIMUM_LINE_COUNT = 10;
    
    private int frame;
    private int line;
    private TextFieldHelper signField;
    private final String[] messages;
    
    public CommonHolographicSignEditingScreen(CommonHolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit"),theSign);
        var size = theSign.contents.size();
        this.messages = new String[Math.max(size,MAXIMUM_LINE_COUNT)];
        Arrays.fill(this.messages, "");
        for (int i = 0; i < size; i++) {
            this.messages[i] = theSign.contents.get(i).getString();
        }
    }

    @Override
    protected void init() {
        super.init();
        var mc = Objects.requireNonNull(this.minecraft, "Minecraft instance is missing while Screen is initializing!");
        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos(this.width / 2 - 100, this.height / 4 + 120)
                .size(200, 20).build());
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
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if(super.charTyped(pCodePoint, pModifiers)) return true;
        this.signField.charTyped(pCodePoint);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(super.keyPressed(keyCode, scanCode, modifiers)) return true;
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
            return this.signField.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Lighting.setupForFlatItems();
        // I don't know, someone please explain why these transforms are necessary???
        var transform = guiGraphics.pose();
        transform.pushPose();
        transform.translate(0.0F, 0.0F, 50.0F);
        transform.scale(93.75F, -93.75F, 93.75F);
        transform.translate(0.0, -1.625, 0.0);

        // Render the text and cursor
        boolean showCursor = this.frame / 6 % 2 == 0;
        transform.translate(0, 1.0 / 3.0, 7.0 / 15.0);
        transform.scale(1F / 96F, -1F / 96F, 1F / 96F);
        int cursorPos = this.signField.getCursorPos();
        int selectionPos = this.signField.getSelectionPos();
        int cursorY = this.line * 10 - this.messages.length * 5;
        for(int line = 0; line < this.messages.length; ++line) {
            String text = this.messages[line];
            if (text != null) {
                if (this.font.isBidirectional()) {
                    text = this.font.bidirectionalShaping(text);
                }
                int xStart = (int)switch (this.textAlign) {
                    case LEFT -> this.width / 10.0;
                    case CENTER -> this.width / 2.0 - this.font.width(text) / 2.0;
                    case RIGHT -> this.width * 0.9 - this.font.width(text);
                };
                guiGraphics.drawString(this.font, text, xStart, line * 10 - this.messages.length * 5, 0xFFFFFF, false);
                if (line == this.line && cursorPos >= 0 && showCursor) {
                    int j1 = this.font.width(text.substring(0, Math.min(cursorPos, text.length())));
                    int cursorX = (int) switch (this.textAlign) {
                        case LEFT -> this.width / 10.0 + j1;
                        case CENTER -> this.width / 2.0 + j1 - this.font.width(text) / 2.0;
                        case RIGHT -> this.width * 0.9F;
                    };
                    if (cursorPos >= text.length() && (!this.colorInput.isFocused() && !this.rotationInput.isFocused())) {
                        guiGraphics.drawString(this.font, "_", cursorX, cursorY, 0xFFFFFF, false);
                    }
                }
            }
        }

        // Render selection highlights
        for(int i = 0; i < this.messages.length; ++i) {
            String text = this.messages[i];
            if (text != null && i == this.line && cursorPos >= 0) {
                int j3 = this.font.width(text.substring(0, Math.min(cursorPos, text.length())));
                int k3 = j3 - this.font.width(text) / 2;
                if (showCursor && cursorPos < text.length()) {
                    guiGraphics.fill(k3, cursorY - 1, k3 + 1, cursorY + 9, 0xFFFFFFFF);
                }

                if (selectionPos != cursorPos) {
                    int l3 = Math.min(cursorPos, selectionPos);
                    int l1 = Math.max(cursorPos, selectionPos);
                    int i2 = this.font.width(text.substring(0, l3)) - this.font.width(text) / 2;
                    int j2 = this.font.width(text.substring(0, l1)) - this.font.width(text) / 2;
                    int k2 = Math.min(i2, j2);
                    int l2 = Math.max(i2, j2);
                    guiGraphics.fill(RenderType.guiTextHighlight(), k2, cursorY, l2, cursorY + 10, -16776961);
                }
            }
        }

        transform.popPose();
        Lighting.setupFor3DItems();
        
    }
}
