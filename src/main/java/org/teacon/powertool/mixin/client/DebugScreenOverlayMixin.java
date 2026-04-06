package org.teacon.powertool.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.client.AccessControlClient;
import org.teacon.powertool.client.CachedModeClient;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Shadow
    private HitResult block;
    
    @Inject(
            method = "getSystemInformation",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getTags()Ljava/util/stream/Stream;"
            )
    )
    private void appendDisplayModeInformation(
            CallbackInfoReturnable<List<String>> cir,
            @Local(index = 9) List<String> list
    ) {
        boolean isDisplayModeEnabled = AccessControlClient.INSTANCE.isDisplayModeEnabledAt(
                ((BlockHitResult) this.block).getBlockPos()
        );
        boolean isCachedModeEnabled = CachedModeClient.INSTANCE.isCachedModeEnabledOn(
                ((BlockHitResult) this.block).getBlockPos()
        );
        list.add(
                "Display Mode: "
                        + (isDisplayModeEnabled ? ChatFormatting.GREEN + "Enabled" : ChatFormatting.RED + "Disabled")
        );
        list.add(
                "Cached Mode: "
                        + ((isCachedModeEnabled) ? ChatFormatting.GREEN + "Enabled" : ChatFormatting.RED + "Disabled")
        );
    }
}
