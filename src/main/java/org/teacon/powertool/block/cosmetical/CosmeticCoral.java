package org.teacon.powertool.block.cosmetical;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.teacon.powertool.block.ICosmeticBlock;
import org.teacon.powertool.block.WithTooltip;

import java.util.function.Consumer;

public interface CosmeticCoral extends ICosmeticBlock, WithTooltip {

    @Override
    default void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("block.powertool.cosmetic_block.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }
}
