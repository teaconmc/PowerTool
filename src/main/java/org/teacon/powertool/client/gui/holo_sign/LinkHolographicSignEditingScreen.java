package org.teacon.powertool.client.gui.holo_sign;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;

import java.util.Objects;

@NonNullByDefault
public class LinkHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<LinkHolographicSignBlockEntity> {
    
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
        int innerPadding = width / 100;
        var mc = Objects.requireNonNull(this.minecraft, "Minecraft instance is missing while Screen is initializing!");
        this.displayInput = new ObjectInputBox<>(mc.font, width / 2 - 150, 100 + innerPadding * 5, 300, 20, Component.literal("The Text: "), ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER);
        this.displayInput.setMaxLength(114514);
        this.displayInput.setValue(display);
        this.displayInput.setResponder(string -> display = string);
        this.displayInput.setFocused(false);
        this.displayInput.setCanLoseFocus(true);
        this.displayInput.setRenderState(false);
        
        this.urlInput = new ObjectInputBox<>(mc.font, width / 2 - 150, 120 + innerPadding * 6, 300, 20, Component.literal("The URL: "), ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER);
        this.urlInput.setMaxLength(114514);
        this.urlInput.setValue(url);
        this.urlInput.setResponder(string -> url = string);
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
    public boolean charTyped(CharacterEvent event) {
        if (super.charTyped(event)) {
            return true;
        }
            if (this.displayInput.charTyped(event)) {
            return true;
        }
        return this.urlInput.charTyped(event);
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        if (this.displayInput.keyPressed(event)) {
            return true;
        }
        return this.urlInput.keyPressed(event);
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.displayInput.mouseClicked(event, doubleClick)) {
            this.displayInput.setFocused(false);
        }
        if (!this.urlInput.mouseClicked(event, doubleClick)) {
            this.urlInput.setFocused(false);
        }
        return super.mouseClicked(event, doubleClick);
    }
    
}
