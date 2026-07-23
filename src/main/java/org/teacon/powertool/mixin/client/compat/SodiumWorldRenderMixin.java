package org.teacon.powertool.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.teacon.powertool.client.CachedModeClient;
import org.teacon.powertool.client.anvilcraft.rendering.CacheableBERenderingPipeline;

import java.util.SortedSet;

@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRenderMixin {
    @WrapOperation(
        method = "extractBlockEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/render/SodiumWorldRenderer;extractBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;FLit/unimi/dsi/fastutil/longs/Long2ObjectMap;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V"
        )
    )
    private static void wrapRenderBlockEntity(
        SodiumWorldRenderer instance,
        BlockEntity blockEntity,
        PoseStack poseStack,
        Camera camera,
        float tickDelta,
        Long2ObjectMap<SortedSet<BlockDestructionProgress>> progression,
        LevelRenderState levelRenderState,
        Operation<Void> original
    ) {
        if (CachedModeClient.INSTANCE.isCachedModeEnabledOn(blockEntity)) {
            CacheableBERenderingPipeline.getInstance()
                .getRenderRegion(ChunkPos.containing(blockEntity.getBlockPos()))
                .addIfPossible(blockEntity);
            return;
        }
        original.call(instance, blockEntity, poseStack, camera, tickDelta, progression, levelRenderState);
    }
}
