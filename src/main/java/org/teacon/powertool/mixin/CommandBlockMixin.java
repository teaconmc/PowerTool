package org.teacon.powertool.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.teacon.powertool.bridge.PeriodCommandBlockBridge;

@Mixin(CommandBlock.class)
public class CommandBlockMixin implements PeriodCommandBlockBridge {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
    private void usePowerToolPeriod(ServerLevel level, BlockPos pos, Block block, int i) {
        level.scheduleTick(pos, block, powerTool$Period(level, pos));
    }

    @Override
    public int powerTool$Period(ServerLevel level, BlockPos pos) {
        return 1;
    }
}
