package org.teacon.powertool.client.anvilcraft.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.apache.commons.lang3.function.TriFunction;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.client.renderer.RenderStateShard.LIGHTMAP;
import static net.minecraft.client.renderer.RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER;
import static net.minecraft.client.renderer.RenderStateShard.TRANSLUCENT_TRANSPARENCY;

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
    private final Map<RenderType, RenderType> cachedRenderTypeConvertions = new HashMap<>();

    public FullyBufferedBufferSource() {
        super(null, null);
    }

    private ByteBufferBuilder getByteBuffer(RenderType renderType) {
        return byteBuffers.computeIfAbsent(renderType, it -> new ByteBufferBuilder(786432));
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return bufferBuilders.computeIfAbsent(
            forceUseBlockRenderTypes(renderType),
            it -> new BufferBuilder(
                getByteBuffer(forceUseBlockRenderTypes(renderType)),
                it.mode,
                it.format
            )
        );
    }

    private RenderType forceUseBlockRenderTypes(RenderType renderType) {
        if (renderType.format == DefaultVertexFormat.NEW_ENTITY && renderType instanceof RenderType.CompositeRenderType compositeRenderType) {
            return cachedRenderTypeConvertions.computeIfAbsent(
                renderType,
                it -> createBlockRenderType(compositeRenderType)
            );
        }
        return renderType;
    }

    private RenderType createBlockRenderType(
        RenderType.CompositeRenderType renderType
    ) {
        RenderType.CompositeState state = renderType.state();
        return RenderType.create(
            "powertool:generated",
            DefaultVertexFormat.BLOCK,
            renderType.mode,
            786432,
            renderType.affectsCrumbling,
            renderType.sortOnUpload,
            RenderType.CompositeState.builder()
                .setCullState(state.cullState)
                .setOutputState(state.outputState)
                .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                .setOutputState(state.outputState)
                .setTextureState(state.textureState)
                .setLightmapState(LIGHTMAP)
                .setTransparencyState(state.transparencyState)
                .createCompositeState(false)
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

    public void upload(
        Function<RenderType, VertexBuffer> vertexBufferGetter,
        Function<RenderType, ByteBufferBuilder> byteBufferSupplier,
        Consumer<Runnable> runner
    ) {
        for (RenderType renderType : bufferBuilders.keySet()) {
            runner.accept(() -> {
                BufferBuilder bufferBuilder = bufferBuilders.get(renderType);
                ByteBufferBuilder byteBuffer = byteBuffers.get(renderType);
                int compiledVertices = bufferBuilder.vertices * renderType.format.getVertexSize();
                if (compiledVertices >= 0) {
                    MeshData mesh = bufferBuilder.build();
                    indexCountMap.put(renderType, renderType.mode.indexCount(bufferBuilder.vertices));
                    if (mesh != null) {
                        if (renderType.sortOnUpload) {
                            MeshData.SortState sortState = mesh.sortQuads(
                                byteBufferSupplier.apply(renderType),
                                RenderSystem.getVertexSorting()
                            );
                            meshSorts.put(
                                renderType,
                                sortState
                            );
                        }
                        VertexBuffer vertexBuffer = vertexBufferGetter.apply(renderType);
                        vertexBuffer.bind();
                        vertexBuffer.upload(mesh);
                        VertexBuffer.unbind();
                    }
                }
                byteBuffer.close();
                bufferBuilders.remove(renderType);
                byteBuffers.remove(renderType);
            });
        }
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
}
