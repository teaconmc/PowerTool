package org.teacon.powertool.compat.iris.mixins;

import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.client.renders.BezierCurveRenderingPipeline;
import org.teacon.powertool.compat.iris.IrisSupport;

@Mixin(BezierCurveRenderingPipeline.BezierCurveChunk.class)
public class BezierCurveChunkMixin {

    @Inject(
        method = "rebuild",
        at = @At("HEAD")
    )
    void handleBegin(CallbackInfo ci) {
        IrisSupport.pushIrisGlobalState();
        ImmediateState.isRenderingLevel = true;
        ImmediateState.skipExtension.set(false);
    }

    @Inject(
        method = "rebuild",
        at = @At("RETURN")
    )
    void handleEnd(CallbackInfo ci) {
        IrisSupport.popIrisGlobalState();
    }
}
