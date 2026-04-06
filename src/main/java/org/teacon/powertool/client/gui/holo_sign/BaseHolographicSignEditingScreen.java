/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.client.gui.holo_sign;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.block.entity.BaseHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.block.holo_sign.SignType;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.Objects;
import java.util.Optional;

public class BaseHolographicSignEditingScreen<T extends BaseHolographicSignBlockEntity> extends Screen {
    protected final T sign;
    
    protected float scale;
    protected int colorInARGB; // White by default
    protected BaseHolographicSignBlockEntity.Align textAlign;
    protected boolean locked;
    protected int xRotation;
    protected int yRotation;
    protected boolean bidirectional;
    protected boolean renderBackground;
    protected boolean dropShadow;
    protected boolean lit;
    
    protected Button changeAlignment;
    protected ObjectInputBox<Integer> colorInput;
    protected Button zOffsetToggle;
    protected EditBox xRotationInput;
    protected EditBox yRotationInput;
    protected ObjectInputBox<Float> zOffsetInput;
    protected Button lockToggle;
    protected Button bidButton;
    protected Button shadowToggle;
    protected Button backgroundToggle;
    protected Button litToggle;
    
    public static Screen creatHoloSignScreen(BlockEntity sign, SignType type) {
        return switch (type) {
            case COMMON ->
                    sign instanceof CommonHolographicSignBlockEntity be ? new CommonHolographicSignEditingScreen(be) : null;
            case URL ->
                    sign instanceof LinkHolographicSignBlockEntity be ? new LinkHolographicSignEditingScreen(be) : null;
            case RAW_JSON ->
                    sign instanceof RawJsonHolographicSignBlockEntity be ? new RawJsonHolographicSignEditingScreen(be) : null;
        };
    }
    
    public BaseHolographicSignEditingScreen(Component title, T theSign) {
        super(title);
        this.colorInARGB = theSign.colorInARGB;
        this.scale = theSign.scale;
        this.textAlign = theSign.align;
        this.locked = theSign.lock;
        this.yRotation = theSign.yRotate;
        this.xRotation = theSign.xRotate;
        this.bidirectional = theSign.bidirectional;
        this.sign = theSign;
        this.renderBackground = theSign.renderBackground;
        this.dropShadow = theSign.dropShadow;
        this.lit = theSign.lit;
    }
    
    public int getDoneButtonY() {
        return (int) (this.height * 0.85F);
    }
    
    @Override
    protected void init() {
        
        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos(this.width / 2 - 100, getDoneButtonY())
                .size(200, 20).build());
        
        int innerPadding = width / 100;
        
        Button scaleDown = new Button.Builder(Component.literal("-"), btn -> this.scale = Math.max(0, this.scale - 0.125F))
                .pos(80, 0)
                .size(20, 20)
                .createNarration(displayed -> Component.translatable("powertool.gui.holographic_sign.scale", displayed.get()))
                .build();
        
        Button scaleUp = new Button.Builder(Component.literal("+"), btn -> this.scale += 0.125f)
                .pos(100, 0)
                .size(20, 20)
                .createNarration(displayed -> Component.translatable("powertool.gui.holographic_sign.scale", displayed.get()))
                .build();
        
        this.changeAlignment = new Button.Builder(this.textAlign.displayName, btn -> {
            this.textAlign = switch (this.textAlign) {
                case LEFT -> BaseHolographicSignBlockEntity.Align.CENTER;
                case CENTER -> BaseHolographicSignBlockEntity.Align.RIGHT;
                case RIGHT -> BaseHolographicSignBlockEntity.Align.LEFT;
            };
            this.changeAlignment.setMessage(this.textAlign.displayName);
        }).pos(120 + innerPadding, 0)
                .size(80, 20)
                .createNarration(displayed -> Component.translatable("powertool.gui.holographic_sign.narration.text_align", displayed.get()))
                .build();
        
        this.shadowToggle = new Button.Builder(toggleMessage("powertool.gui.holo_sign.shadow", dropShadow), btn -> {
            this.dropShadow = !dropShadow;
            this.shadowToggle.setMessage(toggleMessage("powertool.gui.holo_sign.shadow", dropShadow));
        })
                .pos(120 + innerPadding, 20 + innerPadding)
                .size(80, 20)
                .build();
        
        this.backgroundToggle = new Button.Builder(toggleMessage("powertool.gui.holo_sign.background", renderBackground), btn -> {
            this.renderBackground = !renderBackground;
            this.backgroundToggle.setMessage(toggleMessage("powertool.gui.holo_sign.background", renderBackground));
        })
                .pos(40, 20 + innerPadding)
                .size(80, 20)
                .build();
        
        this.colorInput = new ObjectInputBox<>(font, 290 + innerPadding * 3, 0, 50, 20, Component.empty(), ObjectInputBox.RGB_COLOR_VALIDATOR, ObjectInputBox.RGB_COLOR_RESPONDER);
        this.colorInput.setValue(VanillaUtils.hexColorFromInt(colorInARGB));
        this.colorInput.setFocused(false);
        this.colorInput.setMaxLength(8);
        this.colorInput.setTooltip(Tooltip.create(Component.translatable("powertool.gui.bezier_curve.color")));
        this.colorInput.setCanLoseFocus(true);
        
        this.zOffsetInput = new ObjectInputBox<>(font, 70, 80 + innerPadding * 4, 50, 20, Component.empty(), ObjectInputBox.FLOAT_VALIDATOR, ObjectInputBox.FLOAT_RESPONDER);
        this.zOffsetInput.setResponder(str -> {
            if (zOffsetToggle != null) this.zOffsetToggle.setMessage(arrangeMessage());
        });
        this.zOffsetInput.setValue(String.valueOf(sign.zOffset));
        this.zOffsetInput.setFocused(false);
        this.zOffsetInput.setMaxLength(10);
        this.zOffsetInput.setCanLoseFocus(true);
        
        this.zOffsetToggle = new Button.Builder(arrangeMessage(), btn -> {
            var arr = BaseHolographicSignBlockEntity.LayerArrange.formOffset(Objects.requireNonNullElse(zOffsetInput.get(), Float.NaN));
            arr = switch (arr) {
                case FRONT, CUSTOM -> BaseHolographicSignBlockEntity.LayerArrange.CENTER;
                case CENTER -> BaseHolographicSignBlockEntity.LayerArrange.BACK;
                case BACK -> BaseHolographicSignBlockEntity.LayerArrange.FRONT;
            };
            if (zOffsetInput != null) this.zOffsetInput.setValue(String.valueOf(arr.offsetValue));
            this.zOffsetToggle.setMessage(arrangeMessage());
        }).pos(140 + innerPadding, 80 + innerPadding * 4)
                .size(40 + innerPadding * 3, 20)
                .build();
        
        this.yRotationInput = new EditBox(font, 70, 60 + innerPadding * 3, 50, 20, Component.empty());
        this.yRotationInput.setValue(Integer.toString(this.yRotation));
        this.yRotationInput.setResponder((string) -> {
            try {
                var i = Integer.parseInt(string);
                if (yRotation == i) return;
                i = i % 360;
                this.yRotation = i;
            } catch (NumberFormatException ignored) {
            }
        });
        this.yRotationInput.setFocused(false);
        this.yRotationInput.setCanLoseFocus(true);
        
        this.xRotationInput = new EditBox(font, 70, 40 + innerPadding * 2, 50, 20, Component.empty());
        this.xRotationInput.setValue(Integer.toString(this.xRotation));
        this.xRotationInput.setResponder((string) -> {
            try {
                var i = Integer.parseInt(string);
                if (xRotation == i) return;
                i = i % 360;
                this.xRotation = i;
            } catch (NumberFormatException ignored) {
            }
        });
        this.xRotationInput.setFocused(false);
        this.xRotationInput.setCanLoseFocus(true);
        
        this.lockToggle = new Button.Builder(Component.translatable("powertool.gui.holographic_sign.lock." + this.locked), (btn) -> {
            this.locked = !this.locked;
            this.lockToggle.setMessage(Component.translatable("powertool.gui.holographic_sign.lock." + this.locked));
        }).pos(200 + innerPadding * 2, 20 + innerPadding)
                .size(80, 20)
                .build();
        
        var rotateY90n = new Button.Builder(Component.literal("-90"), (btn) -> {
            rotateY(-90);
            this.yRotationInput.setValue(Integer.toString(this.yRotation));
        }).pos(120 + innerPadding, 60 + innerPadding * 3)
                .size(20, 20)
                .build();
        var rotateY45n = new Button.Builder(Component.literal("-45"), (btn) -> {
            rotateY(-45);
            this.yRotationInput.setValue(Integer.toString(this.yRotation));
        }).pos(140 + innerPadding * 2, 60 + innerPadding * 3)
                .size(20, 20)
                .build();
        var rotateY45p = new Button.Builder(Component.literal("+45"), (btn) -> {
            rotateY(45);
            this.yRotationInput.setValue(Integer.toString(this.yRotation));
        }).pos(160 + innerPadding * 3, 60 + innerPadding * 3)
                .size(20, 20)
                .build();
        var rotateY90p = new Button.Builder(Component.literal("+90"), (btn) -> {
            rotateY(90);
            this.yRotationInput.setValue(Integer.toString(this.yRotation));
        }).pos(180 + innerPadding * 4, 60 + innerPadding * 3)
                .size(20, 20)
                .build();
        
        var rotateX90n = new Button.Builder(Component.literal("-90"), (btn) -> {
            rotateX(-90);
            this.xRotationInput.setValue(Integer.toString(this.xRotation));
        }).pos(120 + innerPadding, 40 + innerPadding * 2)
                .size(20, 20)
                .build();
        var rotateX45n = new Button.Builder(Component.literal("-45"), (btn) -> {
            rotateX(-45);
            this.xRotationInput.setValue(Integer.toString(this.xRotation));
        }).pos(140 + innerPadding * 2, 40 + innerPadding * 2)
                .size(20, 20)
                .build();
        var rotateX45p = new Button.Builder(Component.literal("+45"), (btn) -> {
            rotateX(45);
            this.xRotationInput.setValue(Integer.toString(this.xRotation));
        }).pos(160 + innerPadding * 3, 40 + innerPadding * 2)
                .size(20, 20)
                .build();
        var rotateX90p = new Button.Builder(Component.literal("+90"), (btn) -> {
            rotateX(90);
            this.xRotationInput.setValue(Integer.toString(this.xRotation));
        }).pos(180 + innerPadding * 4, 40 + innerPadding * 2)
                .size(20, 20)
                .build();
        
        this.bidButton = new Button.Builder(Component.translatable("powertool.gui.holographic_sign.bidirectional." + bidirectional), (btn) -> {
            this.bidirectional = !this.bidirectional;
            this.bidButton.setMessage(Component.translatable("powertool.gui.holographic_sign.bidirectional." + bidirectional));
        }).pos(200 + innerPadding * 2, 0)
                .size(80, 20)
                .build();
        
        this.litToggle = new Button.Builder(Component.translatable("powertool.gui.holo_sign.lit_" + (lit ? "on" : "off")), (btn) -> {
            this.lit = !this.lit;
            this.litToggle.setMessage(Component.translatable("powertool.gui.holo_sign.lit_" + (lit ? "on" : "off")));
        }).pos(280 + innerPadding * 3, 20 + innerPadding)
                .size(80, 20)
                .build();
        
        this.addRenderableWidget(scaleUp);
        this.addRenderableWidget(scaleDown);
        this.addRenderableWidget(this.changeAlignment);
        this.addRenderableWidget(this.shadowToggle);
        this.addRenderableWidget(this.backgroundToggle);
        this.addRenderableWidget(this.zOffsetToggle);
        this.addRenderableWidget(this.zOffsetInput);
        this.addRenderableWidget(this.zOffsetToggle);
        this.addRenderableWidget(this.colorInput);
        this.addRenderableWidget(this.yRotationInput);
        this.addRenderableWidget(this.xRotationInput);
        this.addRenderableWidget(this.lockToggle);
        this.addRenderableWidget(rotateY90n);
        this.addRenderableWidget(rotateY45n);
        this.addRenderableWidget(rotateY45p);
        this.addRenderableWidget(rotateY90p);
        this.addRenderableWidget(rotateX90n);
        this.addRenderableWidget(rotateX45n);
        this.addRenderableWidget(rotateX45p);
        this.addRenderableWidget(rotateX90p);
        this.addRenderableWidget(this.bidButton);
        this.addRenderableWidget(this.litToggle);
    }
    
    private void rotateY(int degree) {
        var r = this.yRotation + degree;
        if (r < 0) {
            rotateY(360 + degree);
        } else {
            this.yRotation = r % 360;
        }
    }
    
    private void rotateX(int degree) {
        var r = this.xRotation + degree;
        if (r < 0) {
            rotateX(360 + degree);
        } else {
            this.xRotation = r % 360;
        }
    }
    
    protected void writeBackToBE() {
        this.sign.colorInARGB = Optional.ofNullable(colorInput.get()).orElse(0xffffff);
        this.sign.scale = this.scale;
        this.sign.align = this.textAlign;
        this.sign.lock = this.locked;
        this.sign.yRotate = this.yRotation;
        this.sign.xRotate = this.xRotation;
        this.sign.zOffset = Objects.requireNonNullElse(this.zOffsetInput.get(), 0f);
        this.sign.bidirectional = this.bidirectional;
        this.sign.renderBackground = this.renderBackground;
        this.sign.dropShadow = this.dropShadow;
        this.sign.lit = this.lit;
    }
    
    protected Component toggleMessage(String key, boolean state) {
        return Component.translatable(key + (state ? "_on" : "_off"));
    }
    
    public Component arrangeMessage() {
        var value = this.zOffsetInput.get();
        if (value == null) return BaseHolographicSignBlockEntity.LayerArrange.CUSTOM.displayName;
        if (value == 0f) return BaseHolographicSignBlockEntity.LayerArrange.CENTER.displayName;
        if (value == -0.45f) return BaseHolographicSignBlockEntity.LayerArrange.FRONT.displayName;
        if (value == 0.45f) return BaseHolographicSignBlockEntity.LayerArrange.BACK.displayName;
        return BaseHolographicSignBlockEntity.LayerArrange.CUSTOM.displayName;
    }
    
    @Override
    public void removed() {
        //this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.writeBackToBE();
        PacketDistributor.sendToServer(UpdateBlockEntityData.create(sign));
    }
    
    @Override
    public void tick() {
        if (!this.sign.getType().isValid(this.sign.getBlockState())) {
            this.onDone();
        }
    }
    
    protected void onDone() {
        this.sign.setChanged();
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
    
    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (this.colorInput.charTyped(pCodePoint, pModifiers)) return true;
        if (this.yRotationInput.charTyped(pCodePoint, pModifiers)) return true;
        if (this.xRotationInput.charTyped(pCodePoint, pModifiers)) return true;
        if (this.zOffsetInput.charTyped(pCodePoint, pModifiers)) return true;
        return super.charTyped(pCodePoint, pModifiers);
    }
    
    @Override
    public void onClose() {
        this.onDone();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.colorInput.keyPressed(keyCode, scanCode, modifiers)
                || this.yRotationInput.keyPressed(keyCode, scanCode, modifiers)
                || this.xRotationInput.keyPressed(keyCode, scanCode, modifiers)
                || this.zOffsetInput.keyPressed(keyCode, scanCode, modifiers)) {
            // If color input box is active, let that input box handle it
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.colorInput.mouseClicked(mouseX, mouseY, button)) {
            this.colorInput.setFocused(false);
        }
        if (!this.yRotationInput.mouseClicked(mouseX, mouseY, button)) {
            this.yRotationInput.setFocused(false);
        }
        if (!this.xRotationInput.mouseClicked(mouseX, mouseY, button)) {
            this.xRotationInput.setFocused(false);
        }
        if (!this.zOffsetInput.mouseClicked(mouseX, mouseY, button)) {
            this.zOffsetInput.setFocused(false);
        }
        this.setFocused(null);
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Lighting.setupForFlatItems();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int innerPadding = width / 100;
        var color = 0xFFFFFF;
        guiGraphics.drawString(this.font, Component.translatable("powertool.gui.holographic_sign.scale", this.scale), 7, 7, color, true);
        guiGraphics.drawString(font, Component.translatable("powertool.gui.holo_sign.x_rotation"), 17, 47 + innerPadding * 2, color, true);
        guiGraphics.drawString(font, Component.translatable("powertool.gui.holo_sign.y_rotation"), 17, 67 + innerPadding * 3, color, true);
        guiGraphics.drawString(font, Component.translatable("powertool.gui.holo_sign.z_offset"), 17, 87 + innerPadding * 4, color, true);
        Lighting.setupFor3DItems();
        
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
