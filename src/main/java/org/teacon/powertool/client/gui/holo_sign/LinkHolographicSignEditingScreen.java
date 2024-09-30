package org.teacon.powertool.client.gui.holo_sign;

import net.minecraft.network.chat.Component;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;

import java.util.Objects;

public class LinkHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<LinkHolographicSignBlockEntity>{
    
    String display = "";
    String url = "";
    
    ObjectInputBox<String> displayInput;
    ObjectInputBox<String> urlInput;
    
    public LinkHolographicSignEditingScreen(LinkHolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit.link"), theSign);
        display = theSign.displayContent.getString();
        url = theSign.url;
    }
    
    @Override
    protected void init() {
        super.init();
        var mc = Objects.requireNonNull(this.minecraft, "Minecraft instance is missing while Screen is initializing!");
        this.displayInput = new ObjectInputBox<>(mc.font,width/2-150,height/2-60,300,20,Component.literal("The Text: "),ObjectInputBox.PASS_VALIDATOR,ObjectInputBox.PASS_RESPONDER);
        this.displayInput.setMaxLength(114514);
        this.displayInput.setValue(display);
        this.displayInput.setResponder( string -> display = string);
        this.displayInput.setFocused(false);
        this.displayInput.setCanLoseFocus(true);
        this.displayInput.setRenderState(false);
        
        this.urlInput = new ObjectInputBox<>(mc.font,width/2-150,height/2-35,300,20,Component.literal("The URL: "),ObjectInputBox.PASS_VALIDATOR,ObjectInputBox.PASS_RESPONDER);
        this.urlInput.setMaxLength(114514);
        this.urlInput.setValue(url);
        this.urlInput.setResponder( string -> url = string);
        this.urlInput.setFocused(false);
        this.urlInput.setCanLoseFocus(true);
        this.urlInput.setRenderState(false);
        
        this.addRenderableWidget(displayInput);
        this.addRenderableWidget(urlInput);
    }
    
    @Override
    protected void writeBackToBE() {
        super.writeBackToBE();
        sign.displayContent = Component.literal(display);
        sign.url = url;
    }
    
    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (super.charTyped(pCodePoint, pModifiers)) {
            return true;
        }
        if(this.displayInput.charTyped(pCodePoint, pModifiers)){
            return true;
        }
        return this.urlInput.charTyped(pCodePoint, pModifiers);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if(this.displayInput.keyPressed(keyCode, scanCode, modifiers)){
            return true;
        }
        return this.urlInput.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!this.displayInput.mouseClicked(mouseX, mouseY, button)){
            this.displayInput.setFocused(false);
        }
        if(!this.urlInput.mouseClicked(mouseX, mouseY, button)){
            this.urlInput.setFocused(false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
