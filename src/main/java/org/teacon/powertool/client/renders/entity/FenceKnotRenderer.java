package org.teacon.powertool.client.renders.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;
import org.teacon.powertool.entity.FenceKnotEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FenceKnotRenderer extends EntityRenderer<FenceKnotEntity, FenceKnotRenderer.FenceKnotState> {
    
    public FenceKnotRenderer(EntityRendererProvider.Context context) {
        super(context);
        
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
            leashState.slack = false;
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
        poseStack.mulPose(camera.orientation);
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Component tip = Component.translatable("entity.powertool.fence_knot");
        Component tipLine2 = Component.translatable("entity.powertool.fence_knot.tooltip");
        int transparency = ((int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255)) << 24;
        Font font = this.getFont();
        submitNodeCollector.submitText(poseStack,-font.width(tip) / 2.0F, -15, tip.getVisualOrderText(),false, Font.DisplayMode.SEE_THROUGH, state.lightCoords, -1, transparency, 0);
        submitNodeCollector.submitText(poseStack,-font.width(tipLine2) / 2.0F, -5, tip.getVisualOrderText(),false, Font.DisplayMode.SEE_THROUGH, state.lightCoords, -1, transparency, 0);
        poseStack.popPose();
    }
    
    @Override
    public FenceKnotState createRenderState() {
        return new FenceKnotState();
    }
    
}
