package org.teacon.powertool.block.cosmetical;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.teacon.powertool.block.ICosmeticBlock;
import org.teacon.powertool.block.WithTooltip;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class CosmeticSimpleBlock extends Block implements ICosmeticBlock, WithTooltip {
    
    public CosmeticSimpleBlock(Properties p) {
        super(p);
    }
    
    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("block.powertool.cosmetic_block.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
}
