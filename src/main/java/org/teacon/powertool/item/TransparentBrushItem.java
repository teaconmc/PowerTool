package org.teacon.powertool.item;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import org.teacon.powertool.block.ItemDisplayBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TransparentBrushItem extends Item {
    
    public TransparentBrushItem() {
        super(new Properties().stacksTo(1));
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var bs = level.getBlockState(pos);
        if(!level.isClientSide && bs.getBlock() instanceof ItemDisplayBlock){
            bs = bs.setValue(ItemDisplayBlock.INVISIBLE,!bs.getValue(ItemDisplayBlock.INVISIBLE));
            level.setBlock(pos,bs, 18);
        }
        return super.useOn(context);
    }
    
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.powertool.transparent_brush").withStyle(ChatFormatting.GRAY));
    }
}
