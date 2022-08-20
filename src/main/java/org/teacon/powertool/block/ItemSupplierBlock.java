package org.teacon.powertool.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.entity.ItemSupplierBlockEntity;

import java.util.List;

public class ItemSupplierBlock extends BaseEntityBlock {
    public ItemSupplierBlock(Properties prop) {
        super(prop);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.is(this) || super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TranslatableComponent("block.powertool.item_supplier.tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemSupplierBlockEntity(pos, state);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ItemSupplierBlockEntity theBE) {
            ItemStack thing = theBE.theItem.copy();
            thing.setCount(player.isCrouching() ? thing.getMaxStackSize() : 1);
            player.getInventory().add(thing);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ItemSupplierBlockEntity theBE) {
            if (theBE.theItem.isEmpty() && player.getAbilities().instabuild) {
                theBE.theItem = player.getItemInHand(hand).copy();
                if (!level.isClientSide) {
                    theBE.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                }
            } else {
                ItemStack toGive = theBE.theItem.copy();
                toGive.setCount(player.isCrouching() ? toGive.getMaxStackSize() : 1);
                player.getInventory().add(toGive);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
