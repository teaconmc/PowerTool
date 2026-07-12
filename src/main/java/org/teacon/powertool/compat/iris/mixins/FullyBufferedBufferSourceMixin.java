package org.teacon.powertool.compat.iris.mixins;

import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.teacon.powertool.client.anvilcraft.rendering.FullyBufferedBufferSource;
import org.teacon.powertool.compat.iris.IrisSupport;

@Mixin(FullyBufferedBufferSource.class)
public class FullyBufferedBufferSourceMixin {

    @ModifyVariable(
        method = "getBuffer",
        at = @At("HEAD"),
        index = 1,
        argsOnly = true
    )
    private RenderType unwrapIrisRenderType(RenderType renderType) {
        return IrisSupport.unwrapRenderType(renderType);
    }
}
