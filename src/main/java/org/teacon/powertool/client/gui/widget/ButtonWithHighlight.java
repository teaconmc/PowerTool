package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ButtonWithHighlight extends Button {
    
    private final Identifier texture;
    private final int texWidth, texHeight;
    private final int normalU, normalV;
    private final int highlightU, highlightV;
    private final int pressedU, pressedV;
    
    private boolean isBeingPressed;
    private int pressElapsedTime;
    
    private final OnPress onReleaseMoment;
    
    public ButtonWithHighlight(Builder builder, OnPress onRelease,
                               Identifier texture, int texWidth, int texHeight,
                               int normalU, int normalV, int highlightU, int highlightV, int pressedU, int pressedV) {
        super(builder);
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.texture = texture;
        this.normalU = normalU;
        this.normalV = normalV;
        this.highlightU = highlightU;
        this.highlightV = highlightV;
        this.pressedU = pressedU;
        this.pressedV = pressedV;
        this.onReleaseMoment = onRelease;
    }
    
    public void tick() {
        if (this.isBeingPressed && this.pressElapsedTime++ > 50) {
            this.onPress.onPress(this);
        }
    }
    
    @Override
    public void onPress(InputWithModifiers input) {
        super.onPress(input);
        this.isBeingPressed = true;
    }
    
    @Override
    public void onRelease(MouseButtonEvent event) {
        super.onRelease(event);
        this.isBeingPressed = false;
        this.pressElapsedTime = 0;
        if (this.onReleaseMoment != null) {
            this.onReleaseMoment.onPress(this);
        }
    }
    
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.isBeingPressed) {
            graphics.blit(this.texture, this.getX(), this.getY(), this.pressedU, this.pressedV, this.width, this.height, this.texWidth, this.texHeight);
        } else if (this.isHovered) {
            graphics.blit(this.texture, this.getX(), this.getY(), this.highlightU, this.highlightV, this.width, this.height, this.texWidth, this.texHeight);
        } else {
            graphics.blit(this.texture, this.getX(), this.getY(), this.normalU, this.normalV, this.width, this.height, this.texWidth, this.texHeight);
        }
    }
}
