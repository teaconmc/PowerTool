package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.TempleBlock;
import org.teacon.powertool.block.entity.TempleBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@NonNullByDefault
public class TempleRenderer implements BlockEntityRenderer<TempleBlockEntity, TempleRenderer.TempleBERenderState> {
    
    private final ItemModelResolver itemModelResolver;
    
    public TempleRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }
    
    @Override
    public TempleBERenderState createRenderState() {
        return new TempleBERenderState();
    }
    
    @Override
    public void extractRenderState(TempleBlockEntity blockEntity, TempleBERenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.itemState = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(state.itemState, blockEntity.theItem, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) (blockEntity.getBlockPos().asLong()));
        state.facing = blockEntity.getBlockState().getValue(TempleBlock.HORIZONTAL_FACING);
    }
    
    @Override
    public void submit(TempleBERenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        var facing = state.facing;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.7, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(((facing.get2DDataValue() + 2) % 4) * 90));
        poseStack.translate(0, 0, -0.1875);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        state.itemState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
    
    @SuppressWarnings("NotNullFieldNotInitialized")
    public static class TempleBERenderState extends BlockEntityRenderState{
        public ItemStackRenderState itemState;
        public Direction facing;
    }
}
