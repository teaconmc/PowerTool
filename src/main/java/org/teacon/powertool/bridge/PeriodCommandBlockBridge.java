package org.teacon.powertool.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface PeriodCommandBlockBridge {

    int powerToolPeriod(ServerLevel level, BlockPos pos);
}
