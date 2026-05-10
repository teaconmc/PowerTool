package org.teacon.powertool.client.anvilcraft.rendering;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author ZhuRuoLing
 */
public class CachedRegion {
    private final ChunkPos chunkPos;
    private Map<RenderType, GpuBuffer> buffers = new HashMap<>();
    private final Map<RenderType, ByteBufferBuilder> sortBuffers = new HashMap<>();
    private Map<RenderType, MeshData.SortState> meshSortings = new HashMap<>();
    private Reference2IntMap<RenderType> indexCountMap = new Reference2IntOpenHashMap<>();
    private final Set<BlockEntity> blockEntities = new HashSet<>();
    private final CacheableBERenderingPipeline pipeline;
    private final Minecraft minecraft = Minecraft.getInstance();
    @Nullable
    private RebuildTask lastRebuildTask;

    private boolean isEmpty = true;

    public CachedRegion(ChunkPos chunkPos, CacheableBERenderingPipeline pipeline) {
        this.chunkPos = chunkPos;
        this.pipeline = pipeline;
    }

    /**
     * Updates the block entities collection and triggers a rebuild of the region.
     * <p>
     *
     * @see CacheableBERenderingPipeline#update(BlockEntity)
     * @param be The block entity to update.
     */
    public void update(BlockEntity be) {
        if (lastRebuildTask != null) {
            lastRebuildTask.cancel();
        }
        boolean shouldRecompile = blockEntities.removeIf(BlockEntity::isRemoved);
        if (be.isRemoved()) {
            shouldRecompile |= blockEntities.remove(be);
            if (shouldRecompile) {
                pipeline.submitCompileTask(new RebuildTask());
            }
            return;
        }
        shouldRecompile |= blockEntities.add(be);
        if (shouldRecompile) {
            pipeline.submitCompileTask(new RebuildTask());
        }
    }

    /**
     * Handles the removal of a block entity from the system and initiates a cache rebuild.
     * <p>
     * When a block entity is removed, this method is called to update the internal state of the system.
     * It cancels any ongoing rebuild tasks, removes the specified block entity from the collection,
     * cleans up any other removed block entities, and then submits a new rebuild task to the pipeline.
     *
     * @see CacheableBERenderingPipeline#blockRemoved(BlockEntity)
     * @param be The block entity that has been removed.
     */
    public void blockRemoved(BlockEntity be) {
        if (lastRebuildTask != null) {
            lastRebuildTask.cancel();
        }
        boolean removedAny = blockEntities.removeIf(BlockEntity::isRemoved) || blockEntities.remove(be);
        if (removedAny) {
            pipeline.submitCompileTask(new RebuildTask());
        }
    }

    public void render() {
        renderInternal(buffers.keySet());
    }

    public GpuBuffer getBuffer(RenderType renderType, long size) {
        if (buffers.containsKey(renderType)) {
            GpuBuffer buffer = buffers.get(renderType);

            if (buffer.size() < size) {
                buffer = RenderSystem.getDevice().createBuffer(renderType::toString, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC, size);
                buffers.put(renderType, buffer);
            }

            return buffers.get(renderType);
        }
        GpuBuffer vb = RenderSystem.getDevice().createBuffer(renderType::toString, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC, size);
        buffers.put(renderType, vb);
        return vb;
    }

    private ByteBufferBuilder requestSortBuffer(RenderType renderType) {
        if (sortBuffers.containsKey(renderType)) {
            return sortBuffers.get(renderType);
        }
        ByteBufferBuilder builder = new ByteBufferBuilder(4096);
        sortBuffers.put(renderType, builder);
        return builder;
    }

    private void renderInternal(Collection<RenderType> renderTypes) {
        if (isEmpty) return;

        Vec3 cameraPosition = minecraft.gameRenderer.getMainCamera().position();
        int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;

        if (cameraPosition.distanceTo(new Vec3(chunkPos.x() * 16, cameraPosition.y, chunkPos.z() * 16)) > renderDistance) {
            return;
        }

        List<RenderType> renderingOrders = new ArrayList<>(renderTypes);
        renderingOrders.sort(Comparator.comparingInt(a -> (a.sortOnUpload() ? 1 : 0)));

        for (RenderType renderType : renderingOrders) {
            GpuBuffer vb = buffers.get(renderType);
            if (vb == null) continue;
            renderLayer(renderType, vb, cameraPosition);
        }
    }

    public void releaseBuffers() {
        buffers.values().forEach(GpuBuffer::close);
        sortBuffers.values().forEach(ByteBufferBuilder::close);
    }

    private void renderLayer(
            RenderType renderType,
            GpuBuffer vertexBuffer,
            Vec3 cameraPosition
    ) {
        MeshData.SortState sortState = this.meshSortings.get(renderType);
        int indexCount = indexCountMap.getInt(renderType);

        if (indexCount <= 0) return;

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        Consumer<Matrix4fStack> modelViewModifier = renderType.state.layeringTransform.getModifier();

        modelViewStack.pushMatrix();

        if (modelViewModifier != null) {
            modelViewModifier.accept(modelViewStack);
        }

        modelViewStack.translate(
                - (float) cameraPosition.x + chunkPos.getMinBlockX(),
                - (float) cameraPosition.y,
                - (float) cameraPosition.z + chunkPos.getMinBlockZ()
        );

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), renderType.state.textureTransform.getMatrix());
        Map<String, RenderSetup.TextureAndSampler> textures = renderType.state.getTextures();

        GpuBuffer indices;
        VertexFormat.IndexType indexType;
        if (sortState == null) {
            RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(renderType.mode());
            indices = autoIndices.getBuffer(indexCount);
            indexType = autoIndices.type();
        } else {
            ByteBufferBuilder.Result result = sortState.buildSortedIndexBuffer(
                    this.requestSortBuffer(renderType),
                    VertexSorting.byDistance(cameraPosition.toVector3f()));

            if (result != null){
                indices = renderType.state.pipeline.getVertexFormat().uploadImmediateIndexBuffer(result.byteBuffer());
                indexType = sortState.indexType();
                result.close();
            } else {
                RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(renderType.mode());
                indices = autoIndices.getBuffer(indexCount);
                indexType = autoIndices.type();
            }
        }

        RenderTarget renderTarget = renderType.state.outputTarget.getRenderTarget();
        GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : renderTarget.getColorTextureView();
        GpuTextureView depthTexture = renderTarget.useDepth ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView()) : null;

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Immediate draw for " + renderType, colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
            renderPass.setPipeline(renderType.state.pipeline);
            ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
            if (scissorState.enabled()) {
                renderPass.enableScissor(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height());
            }

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertexBuffer);

            for(Map.Entry<String, RenderSetup.TextureAndSampler> entry : textures.entrySet()) {
                renderPass.bindTexture(entry.getKey(), entry.getValue().textureView(), entry.getValue().sampler());
            }

            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, indexCount, 1);
        }

        modelViewStack.popMatrix();
    }

    public void replaceData(Collection<BlockPos> entityPos, ClientLevel clientLevel) {
        List<BlockEntity> blockEntities = entityPos.stream()
                .map(clientLevel::getBlockEntity)
                .filter(Objects::nonNull)
                .toList();
        this.blockEntities.clear();
        this.blockEntities.addAll(blockEntities);
        pipeline.submitCompileTask(new RebuildTask());
    }

    public void forcedUpdate() {
        pipeline.submitCompileTask(new RebuildTask());
    }

    public <E extends BlockEntity> void addIfPossible(E blockEntity) {
        if (!blockEntities.contains(blockEntity)) {
            blockEntities.add(blockEntity);
            pipeline.submitCompileTask(new RebuildTask());
        }
    }

    private class RebuildTask implements Runnable {
        private boolean cancelled = false;

        @Override
        public void run() {
            lastRebuildTask = this;
            PoseStack poseStack = new PoseStack();
            CachedRegion.this.isEmpty = true;
            FullyBufferedBufferSource bufferSource = new FullyBufferedBufferSource();

            for (BlockEntity be : new ArrayList<>(blockEntities)) {
                if (cancelled) {
                    bufferSource.close();
                    return;
                }

                poseStack.pushPose();
                BlockPos pos = be.getBlockPos();
                poseStack.translate(
                        pos.getX() - chunkPos.getMinBlockX(),
                        pos.getY(),
                        pos.getZ() - chunkPos.getMinBlockZ()
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

            CachedRegion.this.isEmpty = bufferSource.isEmpty();
            bufferSource.upload(
                    CachedRegion.this::getBuffer,
                    CachedRegion.this::requestSortBuffer,
                    pipeline::submitUploadTask);

            CachedRegion.this.meshSortings = bufferSource.getMeshSorts();
            CachedRegion.this.indexCountMap = bufferSource.getIndexCountMap();
            lastRebuildTask = null;
        }

        void cancel() {
            cancelled = true;
        }
    }

    public static class EmptyOutlineBufferSource extends OutlineBufferSource {

        public static final EmptyOutlineBufferSource INSTANCE = new EmptyOutlineBufferSource();

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return EmptyVC.INSTANCE;
        }
    }

    public static class EmptyBufferSource extends MultiBufferSource.BufferSource {

        public static final EmptyBufferSource INSTANCE = new EmptyBufferSource();

        protected EmptyBufferSource() {
            super(null, null);
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return EmptyVC.INSTANCE;
        }

        @Override
        public void endBatch() {

        }

        @Override
        public void endBatch(RenderType type) {

        }

        @Override
        public void endLastBatch() {

        }
    }

    public static class EmptyVC implements VertexConsumer {

        public static final EmptyVC INSTANCE = new EmptyVC();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}