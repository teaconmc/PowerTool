/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.client;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.teacon.powertool.block.entity.HolographicSignBlockEntity;
import org.teacon.powertool.network.PowerToolNetwork;
import org.teacon.powertool.network.server.UpdateHolographicSignData;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class HolographicSignEditingScreen extends Screen {
    private final HolographicSignBlockEntity sign;
    private int frame;
    private int line;
    private TextFieldHelper signField;
    private final String[] messages;

    private float scale;
    private int colorInARGB; // White by default
    private HolographicSignBlockEntity.Align textAlign;
    private HolographicSignBlockEntity.Shadow shadowType;
    private HolographicSignBlockEntity.LayerArrange layerArrange;
    
    private boolean locked;
    private int rotation;
    
    private boolean bidirectional;

    private Button changeAlignment;
    private EditBox colorInput;
    private Button zOffsetToggle;
    private Button shadowToggle;
    
    private EditBox rotationInput;
    private Button lockToggle;
    
    private Button rotate90n;
    private Button rotate45n;
    private Button rotate45p;
    private Button rotate90p;
    private Button bidButton;

    public HolographicSignEditingScreen(HolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit"));
        var size = theSign.contents.size();
        this.messages = new String[Math.max(size, UpdateHolographicSignData.MAXIMUM_LINE_COUNT)];
        Arrays.fill(this.messages, "");
        for (int i = 0; i < size; i++) {
            this.messages[i] = theSign.contents.get(i).getString();
        }
        this.colorInARGB = theSign.colorInARGB;
        this.scale = theSign.scale;
        this.textAlign = theSign.align;
        this.shadowType = theSign.shadow;
        this.layerArrange = theSign.arrange;
        this.locked = theSign.lock;
        this.rotation = theSign.rotate;
        this.bidirectional = theSign.bidirectional;
        this.sign = theSign;
    }

    @Override
    protected void init() {
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

        int innerPadding = width / 100;

        Button scaleDown = new Button.Builder(Component.literal("-"), btn -> this.scale = Math.max(0, this.scale - 0.125F))
                .pos(80, 0)
                .size(20, 20)
                .createNarration(displayed -> Component.translatable("powertool.gui.holographic_sign.scale", displayed.get()))
                .build();

        Button scaleUp = new Button.Builder(Component.literal("+"), btn -> this.scale += 0.125)
                .pos(100, 0)
                .size(20, 20)
                .createNarration(displayed -> Component.translatable("powertool.gui.holographic_sign.scale", displayed.get()))
                .build();

        this.changeAlignment = new Button.Builder(this.textAlign.displayName, btn -> {
            this.textAlign = switch (this.textAlign) {
                case LEFT -> HolographicSignBlockEntity.Align.CENTER;
                case CENTER -> HolographicSignBlockEntity.Align.RIGHT;
                case RIGHT -> HolographicSignBlockEntity.Align.LEFT;
            };
            this.changeAlignment.setMessage(this.textAlign.displayName);
        }).pos(120 + innerPadding, 0)
                .size(160, 20)
                .createNarration(displayed -> Component.translatable("powertool.gui.holographic_sign.narration.text_align", displayed.get()))
                .build();

        this.shadowToggle = new Button.Builder(this.shadowType.displayName, btn -> {
            this.shadowType = switch (this.shadowType) {
                case NONE -> HolographicSignBlockEntity.Shadow.DROP;
                case DROP -> HolographicSignBlockEntity.Shadow.PLATE;
                case PLATE -> HolographicSignBlockEntity.Shadow.NONE;
            };
            this.shadowToggle.setMessage(this.shadowType.displayName);
        }).pos(120 + innerPadding, 20 + innerPadding)
                .size(160, 20)
                .createNarration(displayed -> Component.translatable("powertool.gui.holographic_sign.narration.shadow", displayed.get()))
                .build();

        this.colorInput = new EditBox(this.minecraft.font, 280 + innerPadding * 2, 0, 50, 20, Component.empty());
        this.colorInput.setValue("#" + Integer.toHexString(this.colorInARGB));
        this.colorInput.setResponder(string -> {
            TextColor color = TextColor.parseColor(this.colorInput.getValue());
            this.colorInARGB = color == null ? 0xFFFFFFFF : color.getValue() | 0xFF000000;
        });
        this.colorInput.setFocused(false);
        this.colorInput.setCanLoseFocus(true);

        this.zOffsetToggle = new Button.Builder(this.layerArrange.displayName, btn -> {
            this.layerArrange = switch (this.layerArrange) {
                case FRONT -> HolographicSignBlockEntity.LayerArrange.CENTER;
                case CENTER -> HolographicSignBlockEntity.LayerArrange.BACK;
                case BACK -> HolographicSignBlockEntity.LayerArrange.FRONT;
            };
            this.zOffsetToggle.setMessage(this.layerArrange.displayName);
        }).pos(330 + innerPadding * 3, 0)
                .size(80, 20)
                .createNarration(Supplier::get)
                .build();
        
        this.rotationInput = new EditBox(this.minecraft.font,280 + innerPadding * 2, 20 + innerPadding, 50, 20,Component.empty());
        this.rotationInput.setValue(Integer.toString(this.rotation));
        this.rotationInput.setResponder((string) -> {
            try {
                var i = Integer.parseInt(string);
                if(rotation == i)return;
                i = i%360;
                this.rotation = i;
            } catch (NumberFormatException ignored){}
        });
        this.rotationInput.setFocused(false);
        this.rotationInput.setCanLoseFocus(true);
        
        this.lockToggle = new Button.Builder(Component.translatable("powertool.gui.holographic_sign.lock."+this.locked),(btn) -> {
            this.locked = !this.locked;
            this.lockToggle.setMessage(Component.translatable("powertool.gui.holographic_sign.lock."+this.locked));
        }).pos(330 + innerPadding * 3, 20 + innerPadding)
                .size(80,20)
                .createNarration(Supplier::get)
                .build();
        
        this.rotate90n = new Button.Builder(Component.literal("-90"),(btn) -> {
            rotate(-90);
            this.rotationInput.setValue(Integer.toString(this.rotation));
        }).pos(410 + innerPadding * 4, 20 + innerPadding)
                .size(20,20)
                .createNarration(Supplier::get)
                .build();
        this.rotate45n = new Button.Builder(Component.literal("-45"),(btn) -> {
            rotate(-45);
            this.rotationInput.setValue(Integer.toString(this.rotation));
        }).pos(430 + innerPadding * 5, 20 + innerPadding)
                .size(20,20)
                .createNarration(Supplier::get)
                .build();
        this.rotate45p = new Button.Builder(Component.literal("+45"),(btn) -> {
            rotate(45);
            this.rotationInput.setValue(Integer.toString(this.rotation));
        }).pos(450 + innerPadding * 6, 20 + innerPadding)
                .size(20,20)
                .createNarration(Supplier::get)
                .build();
        this.rotate90p = new Button.Builder(Component.literal("+90"),(btn) -> {
            rotate(90);
            this.rotationInput.setValue(Integer.toString(this.rotation));
        }).pos(470 + innerPadding * 7, 20 + innerPadding)
                .size(20,20)
                .createNarration(Supplier::get)
                .build();
        
        this.bidButton = new Button.Builder(Component.translatable("powertool.gui.holographic_sign.bidirectional."+bidirectional),(btn) -> {
            this.bidirectional = !this.bidirectional;
            this.bidButton.setMessage(Component.translatable("powertool.gui.holographic_sign.bidirectional."+bidirectional));
        }).pos(410 + innerPadding * 4, 0)
                .size(80,20)
                .createNarration(Supplier::get)
                .build();
        
        this.addRenderableWidget(scaleUp);
        this.addRenderableWidget(scaleDown);
        this.addRenderableWidget(this.changeAlignment);
        this.addRenderableWidget(this.shadowToggle);
        this.addRenderableWidget(this.zOffsetToggle);
        this.addRenderableWidget(this.colorInput);
        this.addRenderableWidget(this.rotationInput);
        this.addRenderableWidget(this.lockToggle);
        this.addRenderableWidget(this.rotate90n);
        this.addRenderableWidget(this.rotate45n);
        this.addRenderableWidget(this.rotate45p);
        this.addRenderableWidget(this.rotate90p);
        this.addRenderableWidget(this.bidButton);
    }
    
    private void rotate(int degree){
        var r = this.rotation + degree;
        if(r<0){
            rotate(360+degree);
        }
        else {
            this.rotation = r%360;
        }
    }

    @Override
    public void removed() {
        //this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        int last = this.messages.length - 1;
        for (; last >= 0; last--) {
            if (this.messages[last] != null && !this.messages[last].isEmpty()) {
                break;
            }
        }
        var toSend = Arrays.copyOfRange(this.messages, 0, last + 1);
        PowerToolNetwork.channel().send(PacketDistributor.SERVER.with(() -> null),
                new UpdateHolographicSignData(this.sign.getBlockPos(), toSend, this.colorInARGB, this.scale,
                        this.textAlign, this.shadowType, this.layerArrange,this.locked,this.rotation,this.bidirectional));
    }

    @Override
    public void tick() {
        ++this.frame;
        if (!this.sign.getType().isValid(this.sign.getBlockState())) {
            this.onDone();
        }
    }

    private void onDone() {
        this.sign.setChanged();
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (this.colorInput.charTyped(pCodePoint, pModifiers) || this.rotationInput.charTyped(pCodePoint,pModifiers)) {
            return true;
        }
        this.signField.charTyped(pCodePoint);
        return true;
    }

    @Override
    public void onClose() {
        this.onDone();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.colorInput.keyPressed(keyCode, scanCode, modifiers) || this.rotationInput.keyPressed(keyCode, scanCode, modifiers)) {
            // If color input box is active, let that input box handle it
            return true;
        }
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.colorInput.mouseClicked(mouseX, mouseX, button)) {
            this.colorInput.setFocused(false);
        }
        if (!this.rotationInput.mouseClicked(mouseX, mouseX, button)) {
            this.rotationInput.setFocused(false);
        }
        this.setFocused(null);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Lighting.setupForFlatItems();
        this.renderBackground(guiGraphics);
        guiGraphics.drawString(this.font, Component.translatable("powertool.gui.holographic_sign.scale", this.scale), 7, 7, 0xFFFFFF, true);
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
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
