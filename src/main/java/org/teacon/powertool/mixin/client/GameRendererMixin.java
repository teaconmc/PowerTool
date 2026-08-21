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
            int count = 0;
            for(var v : cache.getMap().values()){
                if(v.layout() == null || v.textureTarget() == null || !v.renderState().dirty) continue;
                if(v.textureTarget().width != v.renderState().sizeX || v.textureTarget().height != v.renderState().sizeY){
                    v.textureTarget().resize(v.renderState().sizeX, v.renderState().sizeY);
                }
                OffScreenGuiRenderer.getInstance().render(Objects.requireNonNull(v.textureTarget()), guiGraphicsExtractor -> {
                    guiGraphicsExtractor.pose().pushMatrix();
                    guiGraphicsExtractor.pose().scale(4);
                    var mouseX = v.renderState().mouseX;
                    var mouseY = v.renderState().mouseY;
                    v.layout().setPosition(v.getWidth()/4, v.getHeight()/4);
                    v.layout().drawRecipe(guiGraphicsExtractor, mouseX, mouseY);
                    v.layout().drawOverlays(guiGraphicsExtractor, mouseX, mouseY);
                    guiGraphicsExtractor.extractDeferredElements(mouseX, mouseY, deltaTracker.getGameTimeDeltaPartialTick(false));
                    guiGraphicsExtractor.pose().popMatrix();
                } );
                v.renderState().dirty = false;
                count += 1;
                if(count > 4) break;
            }
            cache.tick();
        }
    }
}
