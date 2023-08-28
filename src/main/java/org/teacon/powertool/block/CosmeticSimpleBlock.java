package org.teacon.powertool.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class CosmeticSimpleBlock extends Block {

    public CosmeticSimpleBlock(Properties p) {
        super(p);
    }

    @Override
    public void appendHoverText(ItemStack item, @Nullable BlockGetter blockGetter, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(Component.translatable("block.powertool.cosmetic_block.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }

}
