package org.teacon.powertool.mixin.client;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

//todo
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

//    @Inject(
//            method = "renderLevel",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/renderer/LevelRenderer;compileSections(Lnet/minecraft/client/Camera;)V"
//            )
//    )
//    void recompileBlockEntities(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
//        CacheableBERenderingPipeline.getInstance().runTasks();
//    }
//
//    @Inject(
//            method = "renderLevel",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/renderer/RenderBuffers;crumblingBufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;",
//                    ordinal = 2
//            )
//    )
//    void renderCachedBE(
//            DeltaTracker deltaTracker,
//            boolean renderBlockOutline,
//            Camera camera,
//            GameRenderer gameRenderer,
//            LightTexture lightTexture,
//            Matrix4f frustumMatrix,
//            Matrix4f projectionMatrix,
//            CallbackInfo ci
//    ) {
//        CacheableBERenderingPipeline.getInstance().render(frustumMatrix, projectionMatrix);
//    }
//
//    @WrapOperation(
//            method = "renderLevel",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
//            )
//    )
//    <E extends BlockEntity> void wrapRenderBlockEntity(
//            BlockEntityRenderDispatcher instance,
//            E blockEntity,
//            float partialTick,
//            PoseStack poseStack,
//            MultiBufferSource bufferSource,
//            Operation<Void> original
//    ) {
//        if (CachedModeClient.INSTANCE.isCachedModeEnabledOn(blockEntity)) {
//            CacheableBERenderingPipeline.getInstance().getRenderRegion(new ChunkPos(blockEntity.getBlockPos()))
//                    .addIfPossible(blockEntity);
//            return;
//        }
//        original.call(instance, blockEntity, partialTick, poseStack, bufferSource);
//    }
}
