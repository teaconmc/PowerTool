package org.teacon.powertool.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.entity.RegisterBlockEntity;

/**
 * 收银台（Register）可接受指定物品并输出红石信号脉冲。
 */
public class RegisterBlock extends BaseEntityBlock {

    public static final MapCodec<RegisterBlock> CODEC = simpleCodec(RegisterBlock::new);

    public RegisterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<RegisterBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RegisterBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof RegisterBlockEntity theBE && !theBE.itemToAccept.isEmpty()){
            boolean accept;
            if (theBE.matchDataComponents) {
                accept = ItemStack.isSameItemSameComponents(stack, theBE.itemToAccept);
            } else {
                accept = stack.is(theBE.itemToAccept.getItem());
            }
            accept &= stack.getCount() >= theBE.itemToAccept.getCount();
            if (accept) {
                stack.shrink(theBE.itemToAccept.getCount());
                // TODO[3TUSK]: Redstone signal / pulse
                return ItemInteractionResult.SUCCESS;
            } else if (!player.getAbilities().instabuild){
                player.displayClientMessage(Component.translatable("block.powertool.register.hud.insufficient"), true);
                return ItemInteractionResult.FAIL;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // TODO[3TUSK]: 打开 GUI
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
