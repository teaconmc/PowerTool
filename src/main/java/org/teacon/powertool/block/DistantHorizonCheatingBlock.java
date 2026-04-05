package org.teacon.powertool.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.teacon.powertool.annotation.NonNullByDefault;

import java.util.function.Consumer;

@NonNullByDefault
public class DistantHorizonCheatingBlock extends Block implements ICosmeticBlock, WithTooltip {
    
    public DistantHorizonCheatingBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1f;
    }
    
    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.powertool.distant_horizon_cheating_block"));
    }
}
