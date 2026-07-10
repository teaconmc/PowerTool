package org.teacon.powertool.client.renders;

import com.mojang.datafixers.util.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.entity.BezierCurveBlockEntity;

import java.util.List;

@NonNullByDefault
public final class BezierCurveBlockRenderer implements BlockEntityRenderer<BezierCurveBlockEntity, BezierCurveBlockRenderer.BezierCurveRenderState> {
    private static final float CONTROL_POINT_HALF_SIZE = 1.0F / 16.0F;
    private static final int CONTROL_POINT_COLOR = 0xFFFF5555;

    public BezierCurveBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BezierCurveRenderState createRenderState() {
        return new BezierCurveRenderState();
    }

    @Override
    public void extractRenderState(
            BezierCurveBlockEntity blockEntity,
            BezierCurveRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        var player = Minecraft.getInstance().player;
        var item = PowerToolBlocks.BEZIER_CURVE_BLOCK.get().asItem();
        if (player == null || !player.getMainHandItem().is(item) && !player.getOffhandItem().is(item)) {
            state.controlPoints = List.of();
            return;
        }
        BlockPos blockPos = blockEntity.getBlockPos();
        float offsetX = blockEntity.worldCoordinate ? blockPos.getX() : 0;
        float offsetY = blockEntity.worldCoordinate ? blockPos.getY() : 0;
        float offsetZ = blockEntity.worldCoordinate ? blockPos.getZ() : 0;
        state.controlPoints = blockEntity.controlPoints.stream()
                .map(point -> new Vector3f(point).sub(offsetX, offsetY, offsetZ))
                .toList();
    }

    @Override
    public void submit(
            BezierCurveRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        for (var controlPoint : state.controlPoints) {
            poseStack.pushPose();
            poseStack.translate(controlPoint.x, controlPoint.y, controlPoint.z);
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), BezierCurveBlockRenderer::renderControlPointCube);
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static void render(BezierCurveBlockEntity blockEntity, ChunkPos chunkPos, VertexConsumer buffer) {
        var model = blockEntity.line;
        if (model == null) return;
        var vertexList = model.vertexAndNormalQuadsList();
        int sideCount = blockEntity.sideCount;
        if (vertexList.size() < (model.line.size() - 1) * sideCount * 4) return;
        var texture = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(blockEntity.texture);
        float uScale = blockEntity.uScale == 0 ? 1 : 1f / blockEntity.uScale;
        float vScale = blockEntity.vScale == 0 ? 1 : 1f / blockEntity.vScale;
        for (int segment = 0; segment < model.line.size() - 1; segment++) {
            float startU = segment * uScale % 1f;
            float endU = startU + uScale;
            for (int side = 0; side < sideCount; side++) {
                float v = side * vScale % 1f;
                int pointer = side * 4 + segment * sideCount * 4;
                putVertex(buffer, blockEntity, chunkPos, vertexList.get(pointer), texture.getU(startU), texture.getV(v));
                putVertex(buffer, blockEntity, chunkPos, vertexList.get(pointer + 1), texture.getU(endU), texture.getV(v));
                putVertex(buffer, blockEntity, chunkPos, vertexList.get(pointer + 2), texture.getU(endU), texture.getV(v + vScale));
                putVertex(buffer, blockEntity, chunkPos, vertexList.get(pointer + 3), texture.getU(startU), texture.getV(v + vScale));
            }
        }
//        for(var i = 0; i < model.vertexAndNormalQuadsList().size()/4; i++){
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4),texture.getU0(),texture.getV0(),-1,packedLight);
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4+1),texture.getU1(),texture.getV0(),-1,packedLight);
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4+2),texture.getU1(),texture.getV1(),-1,packedLight);
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4+3),texture.getU0(),texture.getV1(),-1,packedLight);
//        }
    }

    private static void putVertex(
            VertexConsumer buffer,
            BezierCurveBlockEntity blockEntity,
            ChunkPos chunkPos,
            Pair<Vector3f, Vector3f> vertexAndNormal,
            float u,
            float v
    ) {
        var vertex = vertexAndNormal.getFirst();
        var normal = vertexAndNormal.getSecond();
        BlockPos blockPos = blockEntity.getBlockPos();
        float worldX = vertex.x + (blockEntity.worldCoordinate ? 0 : blockPos.getX());
        float worldY = vertex.y + (blockEntity.worldCoordinate ? 0 : blockPos.getY());
        float worldZ = vertex.z + (blockEntity.worldCoordinate ? 0 : blockPos.getZ());
        var level = blockEntity.getLevel();
        var lightPos = BlockPos.containing(worldX, worldY, worldZ);
        int packedLight = level == null ? 15728880 : LevelRenderer.getLightCoords(level, lightPos);
        buffer.addVertex(worldX - chunkPos.getMinBlockX(), worldY, worldZ - chunkPos.getMinBlockZ())
                .setColor(blockEntity.color)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(normal.x, normal.y, normal.z);
    }

    private static void renderControlPointCube(PoseStack.Pose pose, VertexConsumer buffer) {
        float min = -CONTROL_POINT_HALF_SIZE;
        float max = CONTROL_POINT_HALF_SIZE;
        addQuad(buffer, pose, min, min, min, min, max, min, max, max, min, max, min, min);
        addQuad(buffer, pose, max, min, max, max, max, max, min, max, max, min, min, max);
        addQuad(buffer, pose, min, min, max, min, max, max, min, max, min, min, min, min);
        addQuad(buffer, pose, max, min, min, max, max, min, max, max, max, max, min, max);
        addQuad(buffer, pose, min, max, min, min, max, max, max, max, max, max, max, min);
        addQuad(buffer, pose, min, min, max, min, min, min, max, min, min, max, min, max);
    }

    private static void addQuad(
            VertexConsumer buffer, PoseStack.Pose pose,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4
    ) {
        addVertex(buffer, pose, x1, y1, z1);
        addVertex(buffer, pose, x2, y2, z2);
        addVertex(buffer, pose, x3, y3, z3);
        addVertex(buffer, pose, x4, y4, z4);
    }

    private static void addVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z) {
        buffer.addVertex(pose, x, y, z).setColor(CONTROL_POINT_COLOR);
    }

    public static final class BezierCurveRenderState extends BlockEntityRenderState {
        private List<Vector3f> controlPoints = List.of();
    }
    
    @Override
    public boolean shouldRender(BezierCurveBlockEntity blockEntity, Vec3 cameraPosition) {
        return true;
    }
    
    @Override
    public AABB getRenderBoundingBox(BezierCurveBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
