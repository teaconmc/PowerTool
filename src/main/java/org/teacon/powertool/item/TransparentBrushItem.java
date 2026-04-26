package org.teacon.powertool.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.ItemDisplayBlock;

import java.util.function.Consumer;

@NonNullByDefault
public class TransparentBrushItem extends Item {
    
    public TransparentBrushItem(Properties properties) {
        super(properties.stacksTo(1));
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var bs = level.getBlockState(pos);
        if (!level.isClientSide && bs.getBlock() instanceof ItemDisplayBlock) {
            bs = bs.setValue(ItemDisplayBlock.INVISIBLE, !bs.getValue(ItemDisplayBlock.INVISIBLE));
            level.setBlock(pos, bs, 18);
        }
        return super.useOn(context);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        builder.accept(Component.translatable("tooltip.powertool.transparent_brush").withStyle(ChatFormatting.GRAY));
    }
    
}
