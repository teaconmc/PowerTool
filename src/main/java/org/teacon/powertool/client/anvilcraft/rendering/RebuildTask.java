package org.teacon.powertool.client.anvilcraft.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;

public class RebuildTask implements Runnable {
    private final CachedChunk cachedChunk;
    private boolean cancelled = false;

    public RebuildTask(CachedChunk cachedChunk) {
        this.cachedChunk = cachedChunk;
    }

    @Override
    public void run() {
        cachedChunk.setLastRebuildTask(this);
        PoseStack poseStack = new PoseStack();
        cachedChunk.setEmpty(true);
        FullyBufferedBufferSource bufferSource = new FullyBufferedBufferSource();

        for (BlockEntity be : new ArrayList<>(cachedChunk.blockEntities)) {
            if (cancelled) {
                bufferSource.close();
                return;
            }

            poseStack.pushPose();
            BlockPos pos = be.getBlockPos();
            poseStack.translate(
                pos.getX() - cachedChunk.chunkPos.getMinBlockX(),
                pos.getY(),
                pos.getZ() - cachedChunk.chunkPos.getMinBlockZ()
            );

            SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
            BlockEntityRenderState renderState = Minecraft.getInstance().levelRenderer.blockEntityRenderDispatcher.tryExtractRenderState(be, 0, null, null);

            if (renderState != null) {
                Minecraft.getInstance().levelRenderer.blockEntityRenderDispatcher.submit(renderState, poseStack, submitNodeStorage, Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState);
            }

            FeatureRenderDispatcher dispatcher = new FeatureRenderDispatcher(
                submitNodeStorage,
                Minecraft.getInstance().getModelManager(),
                bufferSource,
                Minecraft.getInstance().getAtlasManager(),
                EmptyOutlineBufferSource.INSTANCE,
                EmptyBufferSource.INSTANCE,
                Minecraft.getInstance().font,
                Minecraft.getInstance().gameRenderer.getGameRenderState()
            );

            dispatcher.renderAllFeatures();
            dispatcher.endFrame();

            poseStack.popPose();
        }

        cachedChunk.setEmpty(bufferSource.isEmpty());
        bufferSource.upload(cachedChunk);
        cachedChunk.replaceMeshData(bufferSource.getMeshSorts(), bufferSource.getIndexCountMap());
        cachedChunk.setLastRebuildTask(null);
    }

    void cancel() {
        cancelled = true;
    }
}
