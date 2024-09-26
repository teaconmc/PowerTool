package org.teacon.powertool.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.teacon.powertool.menu.RegisterMenu;

public class RegisterScreen extends AbstractContainerScreen<RegisterMenu> {

    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("powertool", "textures/gui/register.png");

    public RegisterScreen(RegisterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = 116;
        var matchData = Checkbox.builder(Component.translatable("powertool.gui.register.match_data"), this.font)
                .pos(this.width / 2 - 10, this.height / 2 - 60)
                .build();
        this.addRenderableWidget(matchData);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BG, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
