package org.teacon.powertool.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.teacon.powertool.menu.JEIRecipeDisplayMenu;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class JEIRecipeDisplayScreen extends AbstractContainerScreen<JEIRecipeDisplayMenu> {

    private static final Identifier BG = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public JEIRecipeDisplayScreen(JEIRecipeDisplayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BG, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
