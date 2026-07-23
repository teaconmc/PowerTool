package org.teacon.powertool.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.client.anvilcraft.rendering.CacheableBERenderingPipeline;
import org.teacon.powertool.client.renders.BezierCurveRenderingPipeline;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(
        method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V",
        at = @At("RETURN")
    )
    void updateLevel(ClientLevel level, boolean stopSound, CallbackInfo ci) {
        CacheableBERenderingPipeline.updateLevel(level);
        BezierCurveRenderingPipeline.updateLevel(level);
    }

    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    private void onCreateInstance(GameConfig gameConfig, CallbackInfo ci) {
        CacheableBERenderingPipeline.create();
    }
}
