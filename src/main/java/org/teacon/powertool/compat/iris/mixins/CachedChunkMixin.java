package org.teacon.powertool.compat.iris.mixins;

import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.client.anvilcraft.rendering.CachedChunk;
import org.teacon.powertool.compat.iris.IrisSupport;

@Mixin(CachedChunk.class)
public class CachedChunkMixin {
    @Inject(
        method = "modifyRenderTypeIfNeeded",
        at = @At("HEAD"),
        cancellable = true
    )
    void unwrapIrisRenderType(RenderType rt, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(IrisSupport.unwrapRenderType(rt));
    }
}
