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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.entity.ItemDisplayBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ItemDisplayBlockEntityRenderer implements BlockEntityRenderer<ItemDisplayBlockEntity, ItemDisplayBlockEntityRenderer.ItemDisplayBEState> {
    
    private final ItemModelResolver itemModelResolver;
    
    public ItemDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }
    
    @Override
    public ItemDisplayBEState createRenderState() {
        return new ItemDisplayBEState();
    }
    
    @Override
    public void extractRenderState(ItemDisplayBlockEntity blockEntity, ItemDisplayBEState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.itemState = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(state.itemState, blockEntity.itemToDisplay, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) (blockEntity.getBlockPos().asLong()));
        state.direction = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);
        state.rotation = blockEntity.rotation;
    }
    
    @Override
    public void submit(ItemDisplayBEState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        var direction = state.direction;
        poseStack.translate(direction.getStepX() * -0.4375F, direction.getStepY() * -0.4375F, direction.getStepZ() * -0.4375F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        switch (direction) {
            case DOWN -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
            case UP -> poseStack.mulPose(Axis.XN.rotationDegrees(270));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.rotation));
        state.itemState.submit(poseStack,submitNodeCollector,state.lightCoords, OverlayTexture.NO_OVERLAY,0);
        poseStack.popPose();
    }
    
    public static class ItemDisplayBEState extends BlockEntityRenderState{
        public ItemStackRenderState itemState;
        public Direction direction;
        public int rotation;
    }
}
