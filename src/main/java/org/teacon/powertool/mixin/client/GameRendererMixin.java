package org.teacon.powertool.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.client.PanoramicScreenShotHelper;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render",at = @At("RETURN"))
    public void afterRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci){
        if(PanoramicScreenShotHelper.INSTANCE.takeScreenShot){
            PanoramicScreenShotHelper.INSTANCE.takeScreenShot = false;
            PanoramicScreenShotHelper.INSTANCE.writeImageSection(Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget()));
        }
    }
}
