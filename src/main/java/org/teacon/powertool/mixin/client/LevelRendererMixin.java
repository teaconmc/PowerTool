package org.teacon.powertool.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.CreativeNoClip;
import org.teacon.powertool.client.CachedModeClient;
import org.teacon.powertool.client.anvilcraft.rendering.CacheableBERenderingPipeline;

//todo
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@Inject(
			method = "compileSections",
			at = @At("TAIL")
	)
	void recompileBlockEntities(Camera camera, CallbackInfo ci) {
		CacheableBERenderingPipeline.getInstance().runTasks();
	}

	@Inject(
			method = "lambda$addMainPass$0",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderTranslucentFeatures()V",
					shift = At.Shift.AFTER
			)
	)
	void renderCachedBE(GpuBufferSlice terrainFog, LevelRenderState levelRenderState, ProfilerFiller profiler, ChunkSectionsToRender chunkSectionsToRender, Matrix4fc modelViewMatrix, ResourceHandle entityOutlineTarget, ResourceHandle translucentTarget, ResourceHandle mainTarget, ResourceHandle itemEntityTarget, ResourceHandle particleTarget, boolean renderOutline, CallbackInfo ci) {
		CacheableBERenderingPipeline.getInstance().render();
	}

	@WrapOperation(
			method = "extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/culling/Frustum;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Lnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;"
			)
	)
	<E extends BlockEntity, S extends BlockEntityRenderState> S wrapRenderBlockEntity(BlockEntityRenderDispatcher instance, E blockEntity, float partialTicks, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, @Nullable Frustum frustum, Operation<S> original) {
		if (CachedModeClient.INSTANCE.isCachedModeEnabledOn(blockEntity)) {
			CacheableBERenderingPipeline.getInstance().getRenderRegion(ChunkPos.containing(blockEntity.getBlockPos()))
					.addIfPossible(blockEntity);
			return null;
		}

		return original.call(instance, blockEntity, partialTicks, breakProgress, frustum);
	}
	
	@Redirect(
			method = "update",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z")
	)
	private boolean poseTreatsCreativeNoClipAsSpectator(LocalPlayer player) {
		return CreativeNoClip.canNoClip(player);
	}
}
