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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.teacon.powertool.block.entity.BaseHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.block.holo_sign.SignType;
import org.teacon.powertool.network.server.UpdateBlockEntityData;

import java.util.function.Supplier;

public class BaseHolographicSignEditingScreen<T extends BaseHolographicSignBlockEntity> extends Screen {
    protected final T sign;
    
    protected float scale;
    protected int colorInARGB; // White by default
    protected BaseHolographicSignBlockEntity.Align textAlign;
    protected BaseHolographicSignBlockEntity.Shadow shadowType;
    protected BaseHolographicSignBlockEntity.LayerArrange layerArrange;
    
    protected boolean locked;
    protected int rotation;
    
    protected boolean bidirectional;

    protected Button changeAlignment;
    protected EditBox colorInput;
    protected Button zOffsetToggle;
    protected Button shadowToggle;
    
    protected EditBox rotationInput;
    protected Button lockToggle;
    
    protected Button bidButton;

    public static Screen creatHoloSignScreen(BlockEntity sign, SignType type) {
        return switch (type) {
            case COMMON -> sign instanceof CommonHolographicSignBlockEntity be? new CommonHolographicSignEditingScreen(be) : null;
            case URL -> sign instanceof LinkHolographicSignBlockEntity be ? new LinkHolographicSignEditingScreen(be) : null;
            case RAW_JSON -> sign instanceof RawJsonHolographicSignBlockEntity be ? new RawJsonHolographicSignEditingScreen(be) : null;
        };
    }
    public BaseHolographicSignEditingScreen(Component title,T theSign) {
        super(title);
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
        
        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos(this.width / 2 - 100, this.height / 4 + 120)
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

        this.shadowToggle = new Button.Builder(this.shadowType.displayName, btn -> {
            this.shadowType = switch (this.shadowType) {
                case NONE -> BaseHolographicSignBlockEntity.Shadow.DROP;
                case DROP -> BaseHolographicSignBlockEntity.Shadow.PLATE;
                case PLATE -> BaseHolographicSignBlockEntity.Shadow.NONE;
            };
            this.shadowToggle.setMessage(this.shadowType.displayName);
        }).pos(120 + innerPadding, 20 + innerPadding)
                .size(80, 20)
                .createNarration(displayed -> Component.translatable("powertool.gui.holographic_sign.narration.shadow", displayed.get()))
                .build();

        this.colorInput = new EditBox(this.minecraft.font, 200 + innerPadding * 2, 0, 50, 20, Component.empty());
        this.colorInput.setValue("#" + Integer.toHexString(this.colorInARGB));
        this.colorInput.setResponder(string -> {
            TextColor color = TextColor.parseColor(this.colorInput.getValue()).result().orElse(null);
            this.colorInARGB = color == null ? 0xFFFFFFFF : color.getValue() | 0xFF000000;
        });
        this.colorInput.setFocused(false);
        this.colorInput.setCanLoseFocus(true);

        this.zOffsetToggle = new Button.Builder(this.layerArrange.displayName, btn -> {
            this.layerArrange = switch (this.layerArrange) {
                case FRONT -> BaseHolographicSignBlockEntity.LayerArrange.CENTER;
                case CENTER -> BaseHolographicSignBlockEntity.LayerArrange.BACK;
                case BACK -> BaseHolographicSignBlockEntity.LayerArrange.FRONT;
            };
            this.zOffsetToggle.setMessage(this.layerArrange.displayName);
        }).pos(250 + innerPadding * 3, 0)
                .size(80, 20)
                .createNarration(Supplier::get)
                .build();
        
        this.rotationInput = new EditBox(this.minecraft.font,200 + innerPadding * 2, 20 + innerPadding, 50, 20,Component.empty());
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
        }).pos(250 + innerPadding * 3, 20 + innerPadding)
                .size(80,20)
                .createNarration(Supplier::get)
                .build();
        
        var rotateY90n = new Button.Builder(Component.literal("-90"),(btn) -> {
            rotate(-90);
            this.rotationInput.setValue(Integer.toString(this.rotation));
        }).pos(330 + innerPadding * 4, 20 + innerPadding)
                .size(20,20)
                .createNarration(Supplier::get)
                .build();
        var rotateY45n = new Button.Builder(Component.literal("-45"),(btn) -> {
            rotate(-45);
            this.rotationInput.setValue(Integer.toString(this.rotation));
        }).pos(350 + innerPadding * 5, 20 + innerPadding)
                .size(20,20)
                .createNarration(Supplier::get)
                .build();
        var rotateY45p = new Button.Builder(Component.literal("+45"),(btn) -> {
            rotate(45);
            this.rotationInput.setValue(Integer.toString(this.rotation));
        }).pos(370 + innerPadding * 6, 20 + innerPadding)
                .size(20,20)
                .createNarration(Supplier::get)
                .build();
        var rotateY90p = new Button.Builder(Component.literal("+90"),(btn) -> {
            rotate(90);
            this.rotationInput.setValue(Integer.toString(this.rotation));
        }).pos(390 + innerPadding * 7, 20 + innerPadding)
                .size(20,20)
                .createNarration(Supplier::get)
                .build();
        
        this.bidButton = new Button.Builder(Component.translatable("powertool.gui.holographic_sign.bidirectional."+bidirectional),(btn) -> {
            this.bidirectional = !this.bidirectional;
            this.bidButton.setMessage(Component.translatable("powertool.gui.holographic_sign.bidirectional."+bidirectional));
        }).pos(330 + innerPadding * 4, 0)
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
        this.addRenderableWidget(rotateY90n);
        this.addRenderableWidget(rotateY45n);
        this.addRenderableWidget(rotateY45p);
        this.addRenderableWidget(rotateY90p);
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
    
    protected void writeBackToBE(){
        this.sign.colorInARGB = this.colorInARGB;
        this.sign.scale = this.scale;
        this.sign.align = this.textAlign;
        this.sign.shadow = this.shadowType;
        this.sign.arrange = this.layerArrange;
        this.sign.lock = this.locked;
        this.sign.rotate = this.rotation;
        this.sign.bidirectional = this.bidirectional;
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
        return this.rotationInput.charTyped(pCodePoint, pModifiers);
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
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.colorInput.mouseClicked(mouseX, mouseY, button)) {
            this.colorInput.setFocused(false);
        }
        if (!this.rotationInput.mouseClicked(mouseX, mouseY, button)) {
            this.rotationInput.setFocused(false);
        }
        this.setFocused(null);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Lighting.setupForFlatItems();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, Component.translatable("powertool.gui.holographic_sign.scale", this.scale), 7, 7, 0xFFFFFF, true);
        Lighting.setupFor3DItems();
        
    }
}
