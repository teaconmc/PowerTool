package org.teacon.powertool.client.anvilcraft.rendering;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author ZhuRuoLing
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FullyBufferedBufferSource extends MultiBufferSource.BufferSource implements AutoCloseable {
    private final Map<RenderType, ByteBufferBuilder> byteBuffers = new HashMap<>();
    private final Map<RenderType, BufferBuilder> bufferBuilders = new HashMap<>();
    private final Reference2IntMap<RenderType> indexCountMap = new Reference2IntOpenHashMap<>();
    private final Map<RenderType, MeshData.SortState> meshSorts = new HashMap<>();

    public FullyBufferedBufferSource() {
        super(null, null);
    }

    private ByteBufferBuilder getByteBuffer(RenderType renderType) {
        return byteBuffers.computeIfAbsent(renderType, it -> new ByteBufferBuilder(786432));
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return bufferBuilders.computeIfAbsent(
                renderType,
                it -> new BufferBuilder(
                        getByteBuffer(renderType),
                        it.mode(),
                        it.format()
                )
        );
    }

    public boolean isEmpty() {
        return !bufferBuilders.isEmpty() && bufferBuilders.values().stream().noneMatch(it -> it.vertices > 0);
    }

    @Override
    public void endBatch(RenderType renderType) {
    }

    @Override
    public void endLastBatch() {
    }

    @Override
    public void endBatch() {
    }

    public void upload(VertexBufferHost host) {
        for (RenderType renderType : bufferBuilders.keySet()) {
            host.acceptUploadAction(() -> uploadNow(host, renderType));
        }
    }

    @SuppressWarnings("resource")
    private void uploadNow(VertexBufferHost host, RenderType renderType) {
        BufferBuilder bufferBuilder = bufferBuilders.get(renderType);
        ByteBufferBuilder byteBuffer = byteBuffers.get(renderType);
        int compiledVertices = bufferBuilder.vertices * bufferBuilder.format.getVertexSize();
        if (compiledVertices >= 0) {
            MeshData mesh = bufferBuilder.build();
            indexCountMap.put(renderType, bufferBuilder.mode.indexCount(bufferBuilder.vertices));
            if (mesh != null) {
                if (renderType.sortOnUpload()) {
                    MeshData.SortState sortState = mesh.sortQuads(
                        host.getSortingByteBufferBuilder(renderType),
                        ProjectionType.PERSPECTIVE.vertexSorting()
                    );

                    meshSorts.put(renderType, sortState);
                }

                GpuBuffer vertexBuffer = host.getVertexBuffer(renderType, compiledVertices);
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(vertexBuffer.slice(), mesh.vertexBuffer());
                mesh.close();
            }
        }
        byteBuffer.close();
        bufferBuilders.remove(renderType);
        byteBuffers.remove(renderType);
    }

    public void close(RenderType renderType) {
        ByteBufferBuilder builder = byteBuffers.get(renderType);
        builder.close();
    }

    public Reference2IntMap<RenderType> getIndexCountMap() {
        return indexCountMap;
    }

    public Map<RenderType, MeshData.SortState> getMeshSorts() {
        return meshSorts;
    }

    public void close() {
        byteBuffers.keySet().forEach(this::close);
    }

    public interface VertexBufferHost {
        GpuBuffer getVertexBuffer(RenderType renderType, long size);

        ByteBufferBuilder getSortingByteBufferBuilder(RenderType renderType);

        void acceptUploadAction(Runnable runnable);
    }
}
