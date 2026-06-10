package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.JEIRecipeDisplayBlockEntity;

@NonNullByDefault
public class JEIRecipeDisplayBlockEntityRenderer implements BlockEntityRenderer<JEIRecipeDisplayBlockEntity, JEIRecipeDisplayBlockEntityRenderer.RenderState> {

    public JEIRecipeDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(JEIRecipeDisplayBlockEntity blockEntity, RenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    }

    public static class RenderState extends BlockEntityRenderState {
    }
}
