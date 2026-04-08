package org.teacon.powertool.client.renders.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.entity.AutoVanishBoat;
import org.teacon.powertool.utils.VanillaUtils;

@NonNullByDefault
public class AutoVanishBoatRenderer extends AbstractBoatRenderer {
    
    public AutoVanishBoatRenderer(EntityRendererProvider.Context context) {
        super(context, VanillaUtils.MISSING_TEXTURE);
    }
    
    @Override
    public void submit(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if(state instanceof AutoVanishBoatState avbs){
            var renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(avbs.boatType);
            if(renderer == null) return;
            if(renderer instanceof AbstractBoatRenderer abr){
                abr.submit(state,poseStack,submitNodeCollector,camera);
            }
        }
    }
    
    @Override
    public void extractRenderState(AbstractBoat entity, BoatRenderState state, float partialTicks) {
        if(entity instanceof AutoVanishBoat avb && state instanceof AutoVanishBoatState avbs){
            var entityType = avb.getBoatType();
            avbs.boatType = entityType;
            var renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(entityType);
            if(renderer == null) return;
            if(renderer instanceof AbstractBoatRenderer abr){
                abr.extractRenderState(entity,state,partialTicks);
            }
        }

    }
    
    @Override
    public BoatRenderState createRenderState() {
        return new AutoVanishBoatState();
    }
    
    @Override
    protected EntityModel<BoatRenderState> model() {
        return null;
    }
    
    public static class AutoVanishBoatState extends BoatRenderState {
        public EntityType<?> boatType;
    }
    
}
