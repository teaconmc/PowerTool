package org.teacon.powertool.client.renders.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.client.renders.entity.model.MartingCarEntityModel;
import org.teacon.powertool.entity.MartingCarEntity;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.Arrays;
import java.util.Map;

@NonNullByDefault
public class MartingCarEntityRenderer extends EntityRenderer<MartingCarEntity, MartingCarEntityRenderer.MartingCarState> {
    
    private final Map<MartingCarEntity.Variant, MartingCarEntityModel> variantToModel;
    private static final Map<MartingCarEntity.Variant, Identifier> textures = Map.of(
            MartingCarEntity.Variant.BLUE, VanillaUtils.modRL("textures/entity/marting_blue.png"),
            MartingCarEntity.Variant.RED, VanillaUtils.modRL("textures/entity/marting_red.png"),
            MartingCarEntity.Variant.GREEN, VanillaUtils.modRL("textures/entity/marting_green.png")
    );
    
    public MartingCarEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        
        this.variantToModel = Arrays.stream(MartingCarEntity.Variant.values())
                .collect(
                        ImmutableMap.toImmutableMap(
                                v -> v,
                                v -> createModel(context, v))
                );
    }
    
    private MartingCarEntityModel createModel(EntityRendererProvider.Context context, MartingCarEntity.Variant variant) {
        return new MartingCarEntityModel(context.bakeLayer(getModelLayer(variant)));
    }
    
    private MartingCarEntityModel getBuffer(MartingCarEntity entity) {
        var v = entity.getVariant();
        return variantToModel.get(v);
    }
    
    public static class MartingCarState extends EntityRenderState{
        public MartingCarEntity.Variant variant = MartingCarEntity.Variant.BLUE;
        public float wheelRotation;
        public float steeringRotation;
        public float yRot;
    }
    
    @Override
    public void submit(MartingCarState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        var model = variantToModel.get(state.variant);
        poseStack.pushPose();
        poseStack.translate(0, 1.5, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.scale(1, -1, 1);
        model.updateAnimate(state,state.partialTick);
        submitNodeCollector.submitModel(model,state,poseStack,textures.get(state.variant),state.lightCoords, OverlayTexture.NO_OVERLAY,0,null);
        poseStack.popPose();
    }
    
    @Override
    public MartingCarState createRenderState() {
        return new MartingCarState();
    }
    
    @Override
    public void extractRenderState(MartingCarEntity entity, MartingCarState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.wheelRotation = entity.getWheelRotateRadian();
        state.steeringRotation = entity.getSteeringRotateRadian();
        state.yRot = entity.getYRot(partialTicks);
    }
    
    public static ModelLayerLocation getModelLayer(MartingCarEntity.Variant variant) {
        return switch (variant) {
            case RED -> MartingCarEntityModel.LAYER_RED;
            case GREEN -> MartingCarEntityModel.LAYER_GREEN;
            case BLUE -> MartingCarEntityModel.LAYER_BLUE;
        };
    }
}
