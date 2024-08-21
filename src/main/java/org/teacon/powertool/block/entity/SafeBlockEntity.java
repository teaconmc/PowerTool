package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.teacon.powertool.block.PowerToolBlocks;

public class SafeBlockEntity extends BlockEntity {
    public SafeBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.SAFE_BLOCK_ENTITY.get(), pos, blockState);
    }
}
