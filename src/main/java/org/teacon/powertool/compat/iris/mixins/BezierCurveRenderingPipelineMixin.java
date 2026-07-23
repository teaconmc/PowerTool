package org.teacon.powertool.compat.iris.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.client.renders.BezierCurveRenderingPipeline;
import org.teacon.powertool.compat.iris.IrisSupport;

@Mixin(BezierCurveRenderingPipeline.class)
public abstract class BezierCurveRenderingPipelineMixin {

    @Shadow
    public abstract void rebuildAll();

    @Unique
    private boolean previousShaderEnabled = false;

    @Inject(
        method = "handleIntegration",
        at = @At("HEAD")
    )
    void handleIris(CallbackInfo ci) {
        boolean shaderEnabled = IrisSupport.isShaderEnabled();

        if (previousShaderEnabled != shaderEnabled) {
            this.rebuildAll();
        }

        this.previousShaderEnabled = shaderEnabled;
    }
}
