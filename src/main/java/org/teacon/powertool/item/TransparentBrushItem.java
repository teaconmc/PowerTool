package org.teacon.powertool.item;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
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
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> components, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, components, p_41424_);
        components.add(Component.translatable("tooltip.powertool.transparent_brush").withStyle(ChatFormatting.GRAY));
    }
}
