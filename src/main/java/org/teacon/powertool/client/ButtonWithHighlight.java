package org.teacon.powertool.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ButtonWithHighlight extends Button {

    private final ResourceLocation texture;
    private final int texWidth, texHeight;
    private final int normalU, normalV;
    private final int highlightU, highlightV;
    private final int pressedU, pressedV;

    private boolean isBeingPressed;
    private int pressElapsedTime;

    private final OnPress onReleaseMoment;

    public ButtonWithHighlight(Builder builder, OnPress onRelease,
                               ResourceLocation texture, int texWidth, int texHeight,
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
    public void onPress() {
        super.onPress();
        this.isBeingPressed = true;
    }

    @Override
    public void onRelease(double pMouseX, double pMouseY) {
        super.onRelease(pMouseX, pMouseY);
        this.isBeingPressed = false;
        this.pressElapsedTime = 0;
        if (this.onReleaseMoment != null) {
            this.onReleaseMoment.onPress(this);
        }
    }

    @Override
    public void renderWidget(PoseStack transform, int mouseX, int mouseY, float pPartialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.enableDepthTest();
        if (this.isBeingPressed) {
            blit(transform, this.getX(), this.getY(), this.pressedU, this.pressedV, this.width, this.height, this.texWidth, this.texHeight);
        } else if (this.isHovered) {
            blit(transform, this.getX(), this.getY(), this.highlightU, this.highlightV, this.width, this.height, this.texWidth, this.texHeight);
        } else {
            blit(transform, this.getX(), this.getY(), this.normalU, this.normalV, this.width, this.height, this.texWidth, this.texHeight);
        }
    }
}
