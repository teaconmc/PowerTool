package org.teacon.powertool.client.gui;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.block.entity.RegisterBlockEntity;
import org.teacon.powertool.menu.RegisterMenu;
import org.teacon.powertool.network.server.UpdateBlockEntityData;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegisterScreen extends AbstractContainerScreen<RegisterMenu> {
    
    private static final Identifier BG = Identifier.fromNamespaceAndPath("powertool", "textures/gui/register.png");
    private static final int TEXT_COLOR = 16777215;
    
    private RegisterBlockEntity rbe;
    private Checkbox matchData;
    private Checkbox displaySupply;
    
    public RegisterScreen(RegisterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
    
    @Override
    protected void init() {
        super.init();
        if (this.minecraft != null && this.minecraft.level != null) {
            var be = this.minecraft.level.getBlockEntity(menu.pos);
            if (be instanceof RegisterBlockEntity _rbe) {
                rbe = _rbe;
            }
        }
        this.matchData = Checkbox.builder(Component.translatable("powertool.gui.register.match_data"), this.font)
                .pos(this.leftPos + 80, this.topPos + 25)
                .build();
        this.displaySupply = Checkbox.builder(Component.translatable("powertool.gui.register.display_supply"), this.font)
                .pos(this.leftPos + 80, this.topPos + 45)
                .build();
        if (rbe != null) {
            if (rbe.matchDataComponents) matchData.onPress();
            if (rbe.displaySupply) displaySupply.onPress();
        }
        matchData.textWidget.setColor(TEXT_COLOR);
        displaySupply.textWidget.setColor(TEXT_COLOR);
        this.addRenderableWidget(matchData);
        this.addRenderableWidget(displaySupply);
    }
    
    @Override
    public void removed() {
        if (rbe != null) {
            rbe.itemToAccept = menu.getSlot(0).getItem().copy();
            rbe.itemToSupply = menu.getSlot(1).getItem().copy();
            rbe.matchDataComponents = matchData.selected();
            rbe.displaySupply = displaySupply.selected();
            PacketDistributor.sendToServer(UpdateBlockEntityData.create(rbe));
        }
        super.removed();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        var rev = Component.translatable("powertool.gui.register.rev");
        var sup = Component.translatable("powertool.gui.register.sup");
        guiGraphics.drawString(font, rev, leftPos + 38 - font.width(rev), topPos + 25 + 2, TEXT_COLOR, true);
        guiGraphics.drawString(font, sup, leftPos + 38 - font.width(sup), topPos + 45 + 2, TEXT_COLOR, true);
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BG, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
