package org.teacon.powertool.api;

import net.minecraft.core.BlockPos;

public interface IServerPlayerInteractingBlockPos {
    void powerTool$startInteractingBlockPos(BlockPos pos);
    void powerTool$endInteractingBlockPos();
    BlockPos powerTool$getInteractingBlockPos();
}
