package org.teacon.powertool.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CosmeticBarrel extends CosmeticDirectionalBlock{

    public CosmeticBarrel(Properties p) {
        super(p);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter pLevel, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.powertool.cosmetic_barrel.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }

}
