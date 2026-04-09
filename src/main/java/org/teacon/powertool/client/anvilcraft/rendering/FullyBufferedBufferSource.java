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
    private final Map<net.minecraft.client.renderer.rendertype.RenderType, ByteBufferBuilder> byteBuffers = new HashMap<>();
    private final Map<net.minecraft.client.renderer.rendertype.RenderType, BufferBuilder> bufferBuilders = new HashMap<>();
    private final Reference2IntMap<net.minecraft.client.renderer.rendertype.RenderType> indexCountMap = new Reference2IntOpenHashMap<>();
    private final Map<net.minecraft.client.renderer.rendertype.RenderType, MeshData.SortState> meshSorts = new HashMap<>();

    public FullyBufferedBufferSource() {
        super(null, null);
    }

    private ByteBufferBuilder getByteBuffer(net.minecraft.client.renderer.rendertype.RenderType renderType) {
        return byteBuffers.computeIfAbsent(renderType, it -> new ByteBufferBuilder(786432));
    }

    @Override
    public VertexConsumer getBuffer(net.minecraft.client.renderer.rendertype.RenderType renderType) {
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
    public void endBatch(net.minecraft.client.renderer.rendertype.RenderType renderType) {
    }

    @Override
    public void endLastBatch() {
    }

    @Override
    public void endBatch() {
    }

    public void upload(
            BiFunction<net.minecraft.client.renderer.rendertype.RenderType, Integer, GpuBuffer> vertexBufferGetter,
            Function<net.minecraft.client.renderer.rendertype.RenderType, ByteBufferBuilder> byteBufferSupplier,
            Consumer<Runnable> runner
    ) {
        for (net.minecraft.client.renderer.rendertype.RenderType renderType : bufferBuilders.keySet()) {
            runner.accept(() -> {
                BufferBuilder bufferBuilder = bufferBuilders.get(renderType);
                ByteBufferBuilder byteBuffer = byteBuffers.get(renderType);
                int compiledVertices = bufferBuilder.vertices * renderType.format().getVertexSize();
                if (compiledVertices >= 0) {
                    MeshData mesh = bufferBuilder.build();
                    indexCountMap.put(renderType, renderType.mode().indexCount(bufferBuilder.vertices));
                    if (mesh != null) {
                        if (renderType.sortOnUpload()) {
                            MeshData.SortState sortState = mesh.sortQuads(
                                    byteBufferSupplier.apply(renderType),
                                    ProjectionType.PERSPECTIVE.vertexSorting()
                            );

                            meshSorts.put(
                                    renderType,
                                    sortState
                            );
                        }

                        GpuBuffer vertexBuffer = vertexBufferGetter.apply(renderType, compiledVertices);
                        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(vertexBuffer.slice(), mesh.vertexBuffer());
                    }
                }
                byteBuffer.close();
                bufferBuilders.remove(renderType);
                byteBuffers.remove(renderType);
            });
        }
    }

    public void close(net.minecraft.client.renderer.rendertype.RenderType renderType) {
        ByteBufferBuilder builder = byteBuffers.get(renderType);
        builder.close();
    }

    public Reference2IntMap<net.minecraft.client.renderer.rendertype.RenderType> getIndexCountMap() {
        return indexCountMap;
    }

    public Map<net.minecraft.client.renderer.rendertype.RenderType, MeshData.SortState> getMeshSorts() {
        return meshSorts;
    }

    public void close() {
        byteBuffers.keySet().forEach(this::close);
    }
}
