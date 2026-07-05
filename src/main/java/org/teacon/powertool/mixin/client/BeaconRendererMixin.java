package org.teacon.powertool.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.PowerToolConfig;
import org.teacon.powertool.annotation.NonNullByDefault;

@Mixin(BeaconRenderer.class)
@NonNullByDefault
public class BeaconRendererMixin {
    
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void onSubmit(BeaconRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        if (PowerToolConfig.disableBeaconRender.get()) {
            ci.cancel();
        }
    }
}
