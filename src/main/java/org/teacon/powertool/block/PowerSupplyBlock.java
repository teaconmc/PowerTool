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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.entity.PowerSupplyBlockEntity;
import org.teacon.powertool.menu.PowerSupplyMenu;

public class PowerSupplyBlock extends BaseEntityBlock {
    public PowerSupplyBlock(Properties prop) {
        super(prop);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player p, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (level.getBlockEntity(pos) instanceof PowerSupplyBlockEntity be) {
                NetworkHooks.openScreen((ServerPlayer) p, new PowerSupplyMenu.Provider(be.data),
                        buf -> buf.writeVarInt(be.data.status).writeVarInt(be.data.power));
            }
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PowerSupplyBlockEntity(pPos, pState);
    }

    public static final class Data {
        // Working status flag. 0 represents OFF; 1 represents ON.
        public int status = 1;
        // Amount of energy output per tick
        public int power = 100;
        public Runnable markDirty;
    }
}
