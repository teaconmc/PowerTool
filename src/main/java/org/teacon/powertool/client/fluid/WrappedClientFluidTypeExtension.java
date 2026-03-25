package org.teacon.powertool.client.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrappedClientFluidTypeExtension implements IClientFluidTypeExtensions {
    
    private final IClientFluidTypeExtensions delegate;
    
    public WrappedClientFluidTypeExtension(IClientFluidTypeExtensions delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public int getTintColor() {
        return delegate.getTintColor();
    }
    
    @Override
    public Identifier getStillTexture() {
        return delegate.getStillTexture();
    }
    
    @Override
    public Identifier getFlowingTexture() {
        return delegate.getFlowingTexture();
    }
    
    @Override
    public @Nullable Identifier getOverlayTexture() {
        return delegate.getOverlayTexture();
    }
    
    @Override
    public @Nullable Identifier getRenderOverlayTexture(Minecraft mc) {
        return delegate.getRenderOverlayTexture(mc);
    }
    
    @Override
    public void renderOverlay(Minecraft mc, PoseStack poseStack) {
        delegate.renderOverlay(mc, poseStack);
    }
    
    @Override
    public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
        return delegate.modifyFogColor(camera, partialTick, level, renderDistance, darkenWorldAmount, fluidFogColor);
    }
    
    @Override
    public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
        delegate.modifyFogRender(camera, mode, renderDistance, partialTick, nearDistance, farDistance, shape);
    }
    
    @Override
    public Identifier getStillTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return delegate.getStillTexture(state, getter, pos);
    }
    
    @Override
    public Identifier getFlowingTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return delegate.getFlowingTexture(state, getter, pos);
    }
    
    @Override
    public Identifier getOverlayTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return delegate.getOverlayTexture(state, getter, pos);
    }
    
    @Override
    public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return delegate.getTintColor(state, getter, pos);
    }
    
    @Override
    public int getTintColor(FluidStack stack) {
        return delegate.getTintColor(stack);
    }
    
    @Override
    public Identifier getStillTexture(FluidStack stack) {
        return delegate.getStillTexture(stack);
    }
    
    @Override
    public Identifier getFlowingTexture(FluidStack stack) {
        return delegate.getFlowingTexture(stack);
    }
    
    @Override
    public Identifier getOverlayTexture(FluidStack stack) {
        return delegate.getOverlayTexture(stack);
    }
    
    @Override
    public boolean renderFluid(FluidState fluidState, BlockAndTintGetter getter, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState) {
        return delegate.renderFluid(fluidState, getter, pos, vertexConsumer, blockState);
    }
}
