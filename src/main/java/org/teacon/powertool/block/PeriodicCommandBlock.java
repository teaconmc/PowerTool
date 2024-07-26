package org.teacon.powertool.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.bridge.PeriodCommandBlockBridge;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PeriodicCommandBlock extends CommandBlock implements PeriodCommandBlockBridge {

    public PeriodicCommandBlock(Properties properties, boolean auto) {
        super(auto, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PeriodicCommandBlockEntity(pos, state);
    }

    @Override
    public int powerTool$Period(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PeriodicCommandBlockEntity entity ? entity.getPeriod() : 1;
    }
}
