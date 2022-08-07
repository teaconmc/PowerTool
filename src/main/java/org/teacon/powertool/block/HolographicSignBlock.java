package org.teacon.powertool.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.entity.HolographicSignBlockEntity;
import org.teacon.powertool.network.PowerToolNetwork;
import org.teacon.powertool.network.client.OpenHolographicSignEditor;

public class HolographicSignBlock extends BaseEntityBlock {

    protected HolographicSignBlock(Properties prop) {
        super(prop);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HolographicSignBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            PowerToolNetwork.channel().send(PacketDistributor.PLAYER.with(() -> sp), new OpenHolographicSignEditor(pos));
        }
        return InteractionResult.SUCCESS;
    }
}
