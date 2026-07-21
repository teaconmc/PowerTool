package org.teacon.powertool.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.CreativeBlockBreakUndo;
import org.teacon.powertool.CreativeBlockBreakUndo.Snapshot;
import org.teacon.powertool.annotation.NonNullByDefault;

@NonNullByDefault
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Final
    @Shadow
    protected ServerPlayer player;

    @Unique
    private @Nullable Snapshot powerTool$pendingSnapshot;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void powerTool$captureDestroyedBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.powerTool$pendingSnapshot = CreativeBlockBreakUndo.capture(this.player, pos);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void powerTool$recordDestroyedBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Snapshot snapshot = this.powerTool$pendingSnapshot;
        this.powerTool$pendingSnapshot = null;
        if (snapshot != null && cir.getReturnValue() == true && !this.player.level().getBlockState(pos).equals(snapshot.state())) {
            CreativeBlockBreakUndo.record(this.player, snapshot);
        }
    }
}
