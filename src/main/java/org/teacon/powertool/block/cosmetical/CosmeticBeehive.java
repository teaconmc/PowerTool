package org.teacon.powertool.block.cosmetical;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class CosmeticBeehive extends CosmeticHorizontalDirectionalBlock {
    
    public CosmeticBeehive(Properties p) {
        super(p);
    }
    
    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("block.powertool.cosmetic_beehive.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
    
}
