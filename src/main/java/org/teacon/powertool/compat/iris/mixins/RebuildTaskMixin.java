package org.teacon.powertool.compat.iris.mixins;

import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.client.anvilcraft.rendering.RebuildTask;
import org.teacon.powertool.compat.iris.IrisSupport;

@Mixin(RebuildTask.class)
public class RebuildTaskMixin {
    @Inject(
        method = "run",
        at = @At("HEAD")
    )
    void handleBegin(CallbackInfo ci) {
        IrisSupport.pushIrisGlobalState();
        ImmediateState.isRenderingLevel = true;
        ImmediateState.skipExtension.set(false);
    }

    @Inject(
        method = "run",
        at = @At("RETURN")
    )
    void handleEnd(CallbackInfo ci){
        IrisSupport.popIrisGlobalState();
    }
}
