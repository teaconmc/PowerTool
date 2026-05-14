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
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author ZhuRuoLing
 */
public class CachedChunk implements FullyBufferedBufferSource.VertexBufferHost {
    private final Map<RenderType, GpuBuffer> vertexBuffers = new HashMap<>();
    private final Map<RenderType, GpuBuffer> indexBuffers = new HashMap<>();
    private final Map<RenderType, ByteBufferBuilder> sortBuffers = new HashMap<>();
    private final Minecraft minecraft = Minecraft.getInstance();

    @Nullable
    private RebuildTask lastRebuildTask;
    private Map<RenderType, MeshData.SortState> meshSortings = new HashMap<>();
    private Reference2IntMap<RenderType> indexCountMap = new Reference2IntOpenHashMap<>();

    final ChunkPos chunkPos;
    final Set<BlockEntity> blockEntities = new HashSet<>();
    final CacheableBERenderingPipeline pipeline;

    private boolean isEmpty = true;
    private boolean isFreshMesh = true;

    public CachedChunk(ChunkPos chunkPos, CacheableBERenderingPipeline pipeline) {
        this.chunkPos = chunkPos;
        this.pipeline = pipeline;
    }

    /**
     * Updates the block entities collection and triggers a rebuild of the region.
     * <p>
     *
     * @param be The block entity to update.
     * @see CacheableBERenderingPipeline#update(BlockEntity)
     */
    public void update(BlockEntity be) {
        if (lastRebuildTask != null) {
            lastRebuildTask.cancel();
        }
        boolean shouldRecompile = blockEntities.removeIf(BlockEntity::isRemoved);
        if (be.isRemoved()) {
            shouldRecompile |= blockEntities.remove(be);
            if (shouldRecompile) {
                pipeline.submitCompileTask(new RebuildTask(this));
            }
            return;
        }
        shouldRecompile |= blockEntities.add(be);
        if (shouldRecompile) {
            pipeline.submitCompileTask(new RebuildTask(this));
        }
    }

    /**
     * Handles the removal of a block entity from the system and initiates a cache rebuild.
     * <p>
     * When a block entity is removed, this method is called to update the internal state of the system.
     * It cancels any ongoing rebuild tasks, removes the specified block entity from the collection,
     * cleans up any other removed block entities, and then submits a new rebuild task to the pipeline.
     *
     * @param be The block entity that has been removed.
     * @see CacheableBERenderingPipeline#blockRemoved(BlockEntity)
     */
    public void blockRemoved(BlockEntity be) {
        if (lastRebuildTask != null) {
            lastRebuildTask.cancel();
        }
        boolean removedAny = blockEntities.removeIf(BlockEntity::isRemoved) || blockEntities.remove(be);
        if (removedAny) {
            pipeline.submitCompileTask(new RebuildTask(this));
        }
    }

    public void render() {
        renderInternal(vertexBuffers.keySet());
    }

    public GpuBuffer getBuffer(Map<RenderType, GpuBuffer> buffers, RenderType renderType, long size, int usage) {
        if (buffers.containsKey(renderType)) {
            GpuBuffer buffer = buffers.get(renderType);

            if (buffer.size() < size) {
                buffer = RenderSystem.getDevice().createBuffer(renderType::toString, usage | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC, size);
                GpuBuffer old = buffers.put(renderType, buffer);
                if (old != null) {
                    old.close();
                }
            }

            return buffers.get(renderType);
        }
        GpuBuffer vb = RenderSystem.getDevice().createBuffer(renderType::toString, usage | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC, size);
        buffers.put(renderType, vb);
        return vb;
    }

    public GpuBuffer getVertexBuffer(RenderType renderType, long size) {
        return getBuffer(this.vertexBuffers, renderType, size, GpuBuffer.USAGE_VERTEX);
    }

    public GpuBuffer getIndexBuffer(RenderType renderType, long size) {
        return getBuffer(this.indexBuffers, renderType, size, GpuBuffer.USAGE_INDEX);
    }

    public ByteBufferBuilder getSortingByteBufferBuilder(RenderType renderType) {
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
            GpuBuffer vb = vertexBuffers.get(renderType);
            if (vb == null) continue;
            renderLayer(renderType, vb, cameraPosition);
        }
    }

    public void releaseBuffers() {
        vertexBuffers.values().forEach(GpuBuffer::close);
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

        Matrix4fStack modelViewStack = translateModelViewStack(renderType, cameraPosition);

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), renderType.state.textureTransform.getMatrix());
        Map<String, RenderSetup.TextureAndSampler> textures = renderType.state.getTextures();

        IndexGenerationResult result = getIndexBuffer(renderType, cameraPosition, sortState, indexCount);

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

            for (Map.Entry<String, RenderSetup.TextureAndSampler> entry : textures.entrySet()) {
                renderPass.bindTexture(entry.getKey(), entry.getValue().textureView(), entry.getValue().sampler());
            }

            renderPass.setIndexBuffer(result.indices, result.indexType);
            renderPass.drawIndexed(0, 0, indexCount, 1);
        }

        modelViewStack.popMatrix();
    }

    private @NonNull Matrix4fStack translateModelViewStack(RenderType renderType, Vec3 cameraPosition) {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        Consumer<Matrix4fStack> modelViewModifier = renderType.state.layeringTransform.getModifier();

        modelViewStack.pushMatrix();

        if (modelViewModifier != null) {
            modelViewModifier.accept(modelViewStack);
        }

        modelViewStack.translate(
            -(float) cameraPosition.x + chunkPos.getMinBlockX(),
            -(float) cameraPosition.y,
            -(float) cameraPosition.z + chunkPos.getMinBlockZ()
        );
        return modelViewStack;
    }

    public void replaceData(Collection<BlockPos> entityPos, ClientLevel clientLevel) {
        List<BlockEntity> blockEntities = entityPos.stream()
            .map(clientLevel::getBlockEntity)
            .filter(Objects::nonNull)
            .toList();
        this.blockEntities.clear();
        this.blockEntities.addAll(blockEntities);
        pipeline.submitCompileTask(new RebuildTask(this));
    }

    public void forcedUpdate() {
        pipeline.submitCompileTask(new RebuildTask(this));
    }

    public <E extends BlockEntity> void addIfPossible(E blockEntity) {
        if (!blockEntities.contains(blockEntity)) {
            blockEntities.add(blockEntity);
            pipeline.submitCompileTask(new RebuildTask(this));
        }
    }


    private CachedChunk.IndexGenerationResult getIndexBuffer(RenderType renderType, Vec3 cameraPosition, MeshData.@Nullable SortState sortState, int indexCount) {
        VertexFormat.IndexType indexType;
        GpuBuffer indices;
        if (sortState == null) {
            RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(renderType.mode());
            indices = autoIndices.getBuffer(indexCount);
            indexType = autoIndices.type();
        } else {
            if (isFreshMesh || CacheableBERenderingPipeline.isCameraMoved()) {
                if (isFreshMesh) {
                    isFreshMesh = false;
                }
                Vector3f relativePos = cameraPosition.toVector3f().sub(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());

                ByteBufferBuilder builder = this.getSortingByteBufferBuilder(renderType);
                ByteBufferBuilder.Result result = sortState.buildSortedIndexBuffer(
                    builder,
                    VertexSorting.byDistance(relativePos)
                );


                if (result != null) {
                    indices = getIndexBuffer(renderType, (long) sortState.indexType().bytes * indexCount);
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(indices.slice(), result.byteBuffer());
                    indexType = sortState.indexType();
                    result.close();
                    builder.clear();
                } else {
                    RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(renderType.mode());
                    indices = autoIndices.getBuffer(indexCount);
                    indexType = autoIndices.type();
                }
            } else {
                if (indexBuffers.containsKey(renderType)) {
                    indices = getIndexBuffer(renderType, (long) sortState.indexType().bytes * indexCount);
                    indexType = sortState.indexType();
                } else {
                    RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(renderType.mode());
                    indices = autoIndices.getBuffer(indexCount);
                    indexType = autoIndices.type();
                }

            }
        }
        return new IndexGenerationResult(indices, indexType);
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public void setLastRebuildTask(@Nullable RebuildTask lastRebuildTask) {
        this.lastRebuildTask = lastRebuildTask;
    }

    @Override
    public void acceptUploadAction(Runnable runnable) {
        pipeline.submitUploadTask(runnable);
    }

    public void replaceMeshData(Map<RenderType, MeshData.SortState> meshSorts, Reference2IntMap<RenderType> indexCountMap) {
        this.meshSortings = meshSorts;
        this.indexCountMap = indexCountMap;
        indexBuffers.forEach((_, buffers) -> buffers.close());
        indexBuffers.clear();
        this.isFreshMesh = true;
    }

    private record IndexGenerationResult(GpuBuffer indices, VertexFormat.IndexType indexType) {
    }

}