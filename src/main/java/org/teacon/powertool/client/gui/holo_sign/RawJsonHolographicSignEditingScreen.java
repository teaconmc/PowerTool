package org.teacon.powertool.client.gui.holo_sign;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.ParserUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.NotNull;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.Nullable;

public class RawJsonHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<RawJsonHolographicSignBlockEntity> {
    
    String content;
    @Nullable
    Component forTest = null;
    
    EditBox contentInput;
    
    public RawJsonHolographicSignEditingScreen(RawJsonHolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit.raw_json"), theSign);
        content = theSign.content;
        updateTestComponent();
    }
    
    protected void updateTestComponent(){
        try{
            //noinspection DataFlowIssue
            forTest = ParserUtils.parseJson(Minecraft.getInstance().level.registryAccess(),new StringReader(content), ComponentSerialization.CODEC);
        }catch (Exception e){
            forTest = null;
        }
    }
    
    
    @Override
    protected void init() {
        super.init();
        var mc = Minecraft.getInstance();
        this.contentInput = new EditBox(mc.font,width/2-150,height/2-50,300,20,Component.empty());
        this.contentInput.setMaxLength(114514);
        this.contentInput.setValue(content);
        this.contentInput.setResponder(str -> {
            content = str;
            updateTestComponent();
        });
        this.contentInput.setFocused(false);
        this.contentInput.setCanLoseFocus(true);
        this.addRenderableWidget(contentInput);
    }
    
    @Override
    protected void writeBackToBE() {
        super.writeBackToBE();
        sign.content = content;
        sign.forFilter = forTest == null ? Component.empty() : forTest;
    }
    
    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (super.charTyped(pCodePoint, pModifiers)) {
            return true;
        }
        return this.contentInput.charTyped(pCodePoint, pModifiers);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return this.contentInput.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!this.contentInput.mouseClicked(mouseX, mouseY, button)){
            this.contentInput.setFocused(false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (forTest != null) {
            guiGraphics.fill(width/2-160,height/2-50,width/2-150,height/2-30, VanillaUtils.getColor(0,255,0,255));
        }
        else guiGraphics.fill(width/2-160,height/2-50,width/2-150,height/2-30, VanillaUtils.getColor(255,0,0,255));
    }
}
