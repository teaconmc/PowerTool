package org.teacon.powertool.client.renders.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.leash.LeashKnotModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import org.teacon.powertool.entity.FenceKnotEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FenceKnotRenderer extends EntityRenderer<FenceKnotEntity, FenceKnotRenderer.FenceKnotState> {
    
    private static final Identifier KNOT_LOCATION = Identifier.withDefaultNamespace("textures/entity/lead_knot/lead_knot.png");
    private final LeashKnotModel model;
    
    public FenceKnotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LeashKnotModel(context.bakeLayer(ModelLayers.LEASH_KNOT));
    }
    
    public static class FenceKnotState extends EntityRenderState{
        public List<LeashState> leashStates = new ArrayList<>();
        public boolean renderHint = false;
    }
    
    @Override
    public void extractRenderState(FenceKnotEntity entity, FenceKnotState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        var fromPos = entity.getPos();
        var from = fromPos.getCenter();
        for(var toPos : entity.getConnectTo()){
            var to = toPos.getCenter();
            var leashState = new EntityRenderState.LeashState();
            leashState.start = from;
            leashState.end = to;
            leashState.startBlockLight = this.getBlockLightLevel(entity,fromPos);
            leashState.endBlockLight = this.getBlockLightLevel(entity,toPos);
            leashState.startSkyLight = this.getSkyLightLevel(entity,fromPos);
            leashState.endSkyLight = this.getSkyLightLevel(entity,toPos);
            leashState.slack = true;
            state.leashStates.add(leashState);
        }
        var mc = Minecraft.getInstance();
        var hitResult = mc.hitResult;
        if (mc.player != null && mc.player.getAbilities().instabuild && hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == entity) {
            state.renderHint = true;
        }
    }
    
    @Override
    public void submit(FenceKnotState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        for(var ls : state.leashStates){
            submitNodeCollector.submitLeash(poseStack, ls);
        }
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0,0.25f,0);
        submitNodeCollector.submitModel(this.model, state, poseStack, KNOT_LOCATION, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        if(state.renderHint){
            poseStack.pushPose();
            poseStack.mulPose(camera.orientation);
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Component tip = Component.translatable("entity.powertool.fence_knot");
            Component tipLine2 = Component.translatable("entity.powertool.fence_knot.tooltip");
            int transparency = ((int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255)) << 24;
            Font font = this.getFont();
            submitNodeCollector.submitText(poseStack,-font.width(tip) / 2.0F, -15, tip.getVisualOrderText(),false, Font.DisplayMode.SEE_THROUGH, state.lightCoords, -1, transparency, 0);
            submitNodeCollector.submitText(poseStack,-font.width(tipLine2) / 2.0F, -5, tipLine2.getVisualOrderText(),false, Font.DisplayMode.SEE_THROUGH, state.lightCoords, -1, transparency, 0);
            poseStack.popPose();
        }
    }
    
    @Override
    public FenceKnotState createRenderState() {
        return new FenceKnotState();
    }
    
    @Override
    protected AABB getBoundingBoxForCulling(FenceKnotEntity entity) {
        return AABB.INFINITE;
    }
}
