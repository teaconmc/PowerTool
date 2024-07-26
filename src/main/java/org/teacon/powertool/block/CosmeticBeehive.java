package org.teacon.powertool.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class CosmeticBeehive extends CosmeticHorizontalDirectionalBlock{

    public CosmeticBeehive(Properties p) {
        super(p);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("block.powertool.cosmetic_beehive.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
    

}
