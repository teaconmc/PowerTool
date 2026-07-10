package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.block.entity.BezierCurveBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;

@NonNullByDefault
@EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
public final class BezierCurveRenderingPipeline {
    private static final RenderType RENDER_TYPE = RenderType.create(
            "powertool_bezier_curve",
            RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    .useLightmap()
                    .createRenderSetup()
    );
    @Nullable
    private static BezierCurveRenderingPipeline instance;
    private final ClientLevel level;
    private final Map<ChunkPos, BezierCurveChunk> chunks = new HashMap<>();
    private final Set<ChunkPos> dirtyChunks = new HashSet<>();

    private BezierCurveRenderingPipeline(ClientLevel level) {
        this.level = level;
    }

    public static void updateLevel(ClientLevel level) {
        if (instance != null) {
            instance.releaseBuffers();
        }
        instance = new BezierCurveRenderingPipeline(level);
    }

    @Nullable
    public static BezierCurveRenderingPipeline getInstance() {
        return instance;
    }

    public void update(BezierCurveBlockEntity blockEntity) {
        var chunkPos = ChunkPos.containing(blockEntity.getBlockPos());
        chunks.computeIfAbsent(chunkPos, BezierCurveChunk::new).add(blockEntity.getBlockPos());
        dirtyChunks.add(chunkPos);
    }

    public void remove(BezierCurveBlockEntity blockEntity) {
        var chunkPos = ChunkPos.containing(blockEntity.getBlockPos());
        var chunk = chunks.get(chunkPos);
        if (chunk == null) return;
        chunk.remove(blockEntity.getBlockPos());
        dirtyChunks.add(chunkPos);
    }

    public void runTasks() {
        if (dirtyChunks.isEmpty()) return;
        var chunksToRebuild = List.copyOf(dirtyChunks);
        dirtyChunks.clear();
        for (var chunkPos : chunksToRebuild) {
            var chunk = chunks.get(chunkPos);
            if (chunk != null) {
                chunk.rebuild(level);
            }
        }
    }

    public void render() {
        for (var chunk : chunks.values()) {
            chunk.render();
        }
    }

    private void unloadChunk(ChunkPos chunkPos) {
        dirtyChunks.remove(chunkPos);
        var chunk = chunks.remove(chunkPos);
        if (chunk != null) {
            chunk.releaseBuffer();
        }
    }

    private void loadChunk(ChunkAccess chunk) {
        var blockPositions = new ArrayList<BlockPos>();
        for (var bezierCurveChunk : chunks.values()) {
            for (var blockPos : bezierCurveChunk.blockPositions) {
                var blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity instanceof BezierCurveBlockEntity bezierCurveBlockEntity
                        && bezierCurveBlockEntity.affectsChunk(chunk.getPos())) {
                    blockPositions.add(blockPos);
                }
            }
        }
        chunk.setData(PowerToolAttachments.BEZIER_CURVES, List.copyOf(blockPositions));
    }

    private void sectionRebuilt(Level eventLevel, BlockPos sectionOrigin) {
        if (eventLevel != level) return;
        var chunk = level.getChunkAt(sectionOrigin);
        var blockPositions = chunk.getExistingDataOrNull(PowerToolAttachments.BEZIER_CURVES);
        if (blockPositions == null || blockPositions.isEmpty()) return;
        var sectionPos = SectionPos.of(sectionOrigin);
        var activeBlockPositions = new ArrayList<BlockPos>();
        for (var blockPos : blockPositions) {
            var blockEntity = level.getBlockEntity(blockPos);
            if (!(blockEntity instanceof BezierCurveBlockEntity bezierCurveBlockEntity)
                    || !bezierCurveBlockEntity.affectsChunk(chunk.getPos())) {
                continue;
            }
            activeBlockPositions.add(blockPos);
            if (bezierCurveBlockEntity.affectsSection(sectionPos)) {
                dirtyChunks.add(ChunkPos.containing(blockPos));
            }
        }
        if (activeBlockPositions.size() != blockPositions.size()) {
            chunk.setData(PowerToolAttachments.BEZIER_CURVES, List.copyOf(activeBlockPositions));
        }
    }

    private void releaseBuffers() {
        for (var chunk : chunks.values()) {
            chunk.releaseBuffer();
        }
        chunks.clear();
        dirtyChunks.clear();
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide() || instance == null) return;
        instance.unloadChunk(event.getChunk().getPos());
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getLevel().isClientSide() || instance == null) return;
        instance.loadChunk(event.getChunk());
    }

    @SubscribeEvent
    public static void onSectionGeometry(AddSectionGeometryEvent event) {
        if (instance == null) return;
        instance.sectionRebuilt(event.getLevel(), event.getSectionOrigin());
    }

    private static final class BezierCurveChunk {
        private final Minecraft minecraft = Minecraft.getInstance();
        private final ChunkPos chunkPos;
        private final Set<BlockPos> blockPositions = new HashSet<>();
        @Nullable
        private GpuBuffer vertexBuffer;
        private int indexCount;

        private BezierCurveChunk(ChunkPos chunkPos) {
            this.chunkPos = chunkPos;
        }

        private void add(BlockPos blockPos) {
            blockPositions.add(blockPos.immutable());
        }

        private void remove(BlockPos blockPos) {
            blockPositions.remove(blockPos);
        }

        private void rebuild(ClientLevel level) {
            try (var byteBuffer = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE)) {
                var builder = new BufferBuilder(byteBuffer, RENDER_TYPE.mode(), RENDER_TYPE.format());
                for (var blockPos : new ArrayList<>(blockPositions)) {
                    var blockEntity = level.getBlockEntity(blockPos);
                    if (blockEntity instanceof BezierCurveBlockEntity bezierCurveBlockEntity) {
                        BezierCurveBlockRenderer.render(bezierCurveBlockEntity, chunkPos, builder);
                    } else {
                        blockPositions.remove(blockPos);
                    }
                }
                var mesh = builder.build();
                if (mesh == null) {
                    indexCount = 0;
                    return;
                }
                upload(mesh);
            }
        }

        private void upload(MeshData mesh) {
            try (mesh) {
                long size = mesh.vertexBuffer().remaining();
                if (vertexBuffer == null || vertexBuffer.size() < size) {
                    var replacement = RenderSystem.getDevice().createBuffer(
                            () -> "PowerTool Bezier Curve " + chunkPos,
                            GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC,
                            size
                    );
                    if (vertexBuffer != null) {
                        vertexBuffer.close();
                    }
                    vertexBuffer = replacement;
                }
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(vertexBuffer.slice(), mesh.vertexBuffer());
                indexCount = mesh.drawState().indexCount();
            }
        }

        private void render() {
            if (vertexBuffer == null || indexCount <= 0) return;
            var cameraPosition = minecraft.gameRenderer.getMainCamera().position();
            int renderDistance = minecraft.options.getEffectiveRenderDistance() * 16;
            var chunkCenter = new Vec3(chunkPos.getMiddleBlockX(), cameraPosition.y, chunkPos.getMiddleBlockZ());
            if (cameraPosition.distanceTo(chunkCenter) > renderDistance) return;
            var modelViewStack = translateModelViewStack(cameraPosition);
            try {
                var dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                        new Vector3f(),
                        RENDER_TYPE.state.textureTransform.getMatrix()
                );
                Map<String, RenderSetup.TextureAndSampler> textures = RENDER_TYPE.state.getTextures();
                RenderTarget renderTarget = RENDER_TYPE.state.outputTarget.getRenderTarget();
                GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null
                        ? RenderSystem.outputColorTextureOverride
                        : renderTarget.getColorTextureView();
                GpuTextureView depthTexture = renderTarget.useDepth
                        ? (RenderSystem.outputDepthTextureOverride != null
                        ? RenderSystem.outputDepthTextureOverride
                        : renderTarget.getDepthTextureView())
                        : null;
                try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "PowerTool Bezier Curve " + chunkPos, colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
                    renderPass.setPipeline(RENDER_TYPE.state.pipeline);
                    ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
                    if (scissorState.enabled()) {
                        renderPass.enableScissor(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height());
                    }
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                    renderPass.setVertexBuffer(0, vertexBuffer);
                    for (var entry : textures.entrySet()) {
                        renderPass.bindTexture(entry.getKey(), entry.getValue().textureView(), entry.getValue().sampler());
                    }
                    var indices = RenderSystem.getSequentialBuffer(RENDER_TYPE.mode());
                    renderPass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
                    renderPass.drawIndexed(0, 0, indexCount, 1);
                }
            } finally {
                modelViewStack.popMatrix();
            }
        }

        private Matrix4fStack translateModelViewStack(Vec3 cameraPosition) {
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            Consumer<Matrix4fStack> modifier = RENDER_TYPE.state.layeringTransform.getModifier();
            modelViewStack.pushMatrix();
            if (modifier != null) {
                modifier.accept(modelViewStack);
            }
            modelViewStack.translate(
                    -(float) cameraPosition.x + chunkPos.getMinBlockX(),
                    -(float) cameraPosition.y,
                    -(float) cameraPosition.z + chunkPos.getMinBlockZ()
            );
            return modelViewStack;
        }

        private void releaseBuffer() {
            if (vertexBuffer != null) {
                vertexBuffer.close();
                vertexBuffer = null;
            }
            indexCount = 0;
        }
    }
}
