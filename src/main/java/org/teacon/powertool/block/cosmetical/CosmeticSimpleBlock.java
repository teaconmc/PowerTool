package org.teacon.powertool.block.cosmetical;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.teacon.powertool.block.CosmeticBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class CosmeticSimpleBlock extends Block implements CosmeticBlock {

    public CosmeticSimpleBlock(Properties p) {
        super(p);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("block.powertool.cosmetic_block.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
    
}
