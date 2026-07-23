package org.teacon.powertool.item;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.teacon.powertool.block.WithTooltip;

import java.util.function.Consumer;

public class CosmeticStandingAndWallBlockItem extends StandingAndWallBlockItem {

    public CosmeticStandingAndWallBlockItem(Block floorBlock, Block wallBlock, Direction attachmentDirection,
                                            Properties properties) {
        super(floorBlock, wallBlock, attachmentDirection, properties.useBlockDescriptionPrefix());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        if (this.getBlock() instanceof WithTooltip withTooltip) {
            withTooltip.appendHoverText(stack, context, display, builder, flag);
        }
    }
}
