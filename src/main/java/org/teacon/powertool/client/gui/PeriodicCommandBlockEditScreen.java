package org.teacon.powertool.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.network.server.SetCommandBlockPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PeriodicCommandBlockEditScreen extends CommandBlockEditScreen {
    
    private static final Component PERIOD = Component.translatable("powertool.gui.period");
    
    private EditBox periodBox;
    
    public PeriodicCommandBlockEditScreen(PeriodicCommandBlockEntity blockEntity) {
        super(blockEntity);
    }
    
    @Override
    protected void init() {
        super.init();
        this.periodBox = this.addRenderableWidget(
                new EditBox(this.font, this.width / 2 - 150 + (300 - 40), 105, 40, 20, Component.empty())
        );
        this.periodBox.setValue("10");
        this.modeButton.active = false;
        this.modeButton.visible = false;
    }
    
    @Override
    public void updateGui() {
        super.updateGui();
        this.periodBox.setValue(String.valueOf(((PeriodicCommandBlockEntity) autoCommandBlock).getPeriod()));
        this.mode = CommandBlockEntity.Mode.AUTO;
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.text(this.font, PERIOD, this.width / 2 - 150 + (300 - 40), 95, 10526880);;
    }
    
    @Override
    protected void populateAndSendPacket() {
        super.populateAndSendPacket();
        try {
            var period = Integer.parseInt(this.periodBox.getValue());
            ClientPacketDistributor.sendToServer(new SetCommandBlockPacket(this.autoCommandBlock.getBlockPos(), period));
        } catch (NumberFormatException ignored) {
        }
    }
}
