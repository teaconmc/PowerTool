package org.teacon.powertool.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BaseCommandBlock;
import net.neoforged.neoforge.network.PacketDistributor;
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
    }

    @Override
    public void updateGui() {
        super.updateGui();
        this.periodBox.setValue(String.valueOf(((PeriodicCommandBlockEntity) autoCommandBlock).getPeriod()));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        guiGraphics.drawString(this.font, PERIOD, this.width / 2 - 150 + (300 - 40), 95, 10526880);
    }

    //todo 修复命令方块使其正常工作
    @Override
    protected void populateAndSendPacket(BaseCommandBlock baseCommandBlock) {
        super.populateAndSendPacket(baseCommandBlock);
        try {
            var period = Integer.parseInt(this.periodBox.getValue());
            PacketDistributor.sendToServer(new SetCommandBlockPacket(
                BlockPos.containing(baseCommandBlock.getPosition()), period
            ));
        } catch (NumberFormatException ignored) {
        }
    }
}
