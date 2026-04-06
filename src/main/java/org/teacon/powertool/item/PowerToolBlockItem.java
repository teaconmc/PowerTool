package org.teacon.powertool.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.WithTooltip;

import java.util.function.Consumer;

@NonNullByDefault
public class PowerToolBlockItem extends BlockItem {
    
    public PowerToolBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        if (this.getBlock() instanceof WithTooltip wt) {
            wt.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        }
    }
}
