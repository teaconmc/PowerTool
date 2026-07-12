package org.teacon.powertool.compat.iris.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.client.anvilcraft.rendering.CacheableBERenderingPipeline;
import org.teacon.powertool.compat.iris.IrisSupport;

@Mixin(CacheableBERenderingPipeline.class)
public abstract class CachedBlockEntityRenderingPipelineMixin {

    @Shadow
    public abstract void forcedUpdate();

    @Unique
    private boolean previousShaderEnabled = false;

    @Inject(
        method = "handleIntegration",
        at = @At("HEAD")
    )
    void handleIris(CallbackInfo ci) {
        boolean shaderEnabled = IrisSupport.isShaderEnabled();

        if (previousShaderEnabled != shaderEnabled) {
            this.forcedUpdate();
        }

        this.previousShaderEnabled = shaderEnabled;
    }
}
