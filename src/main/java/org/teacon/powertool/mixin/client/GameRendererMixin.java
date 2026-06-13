package org.teacon.powertool.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.client.renders.JEIRecipeDisplayBlockEntityRenderer;
import org.teacon.powertool.client.renders.OffScreenGuiRenderer;

import java.util.Objects;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

//    @Inject(method = "render", at = @At("TAIL"))
//    private void afterRender(CallbackInfo ci) {
//        var cache = JEIRecipeDisplayBlockEntityRenderer.recipeLayoutCache;
//        if (cache != null) {
//            cache.tick();
//        }
//    }
    
    @Inject(method = "render", at = @At("HEAD"))
    private void beforeRender(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        var cache = JEIRecipeDisplayBlockEntityRenderer.recipeLayoutCache;
        if (cache != null) {
            for(var v : cache.getMap().values()){
                if(v.layout() == null || !v.dirty().get()) continue;
                OffScreenGuiRenderer.getInstance().render(Objects.requireNonNull(v.textureTarget()), guiGraphicsExtractor -> {
                    v.layout().drawRecipe(guiGraphicsExtractor, 0, 0);
                    v.layout().drawOverlays(guiGraphicsExtractor, 0 ,0);
                } );
                v.dirty().set(false);
            }
            cache.tick();
        }
    }
}
