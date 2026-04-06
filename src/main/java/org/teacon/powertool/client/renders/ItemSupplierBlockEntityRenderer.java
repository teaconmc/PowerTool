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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.ItemSupplierBlockEntity;
import org.teacon.powertool.client.ClientEvents;

@NonNullByDefault
public class ItemSupplierBlockEntityRenderer implements BlockEntityRenderer<ItemSupplierBlockEntity, ItemSupplierBlockEntityRenderer.ItemSupplierBEState> {
    
    private final ItemModelResolver itemModelResolver;
    
    public ItemSupplierBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }
    
    @Override
    public ItemSupplierBEState createRenderState() {
        return new ItemSupplierBEState();
    }
    
    @Override
    public void extractRenderState(ItemSupplierBlockEntity blockEntity, ItemSupplierBEState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.itemState = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(state.itemState, blockEntity.theItem, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) (blockEntity.getBlockPos().asLong()));
        state.partialTicks = partialTicks;
    }
    
    @Override
    public void submit(ItemSupplierBEState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        float rotation = ClientEvents.tickCount + state.partialTicks;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.625F, 0.625F, 0.625F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        state.itemState.submit(poseStack,submitNodeCollector,state.lightCoords, OverlayTexture.NO_OVERLAY,-1);
        poseStack.popPose();
    }
    
    @SuppressWarnings("NotNullFieldNotInitialized")
    public static class ItemSupplierBEState extends BlockEntityRenderState{
        public ItemStackRenderState itemState;
        public float partialTicks;
    }
}
