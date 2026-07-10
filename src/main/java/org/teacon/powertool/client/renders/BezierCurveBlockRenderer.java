package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.block.entity.BezierCurveBlockEntity;
import org.teacon.powertool.utils.math.Line3f;

import java.util.ArrayList;
import java.util.List;

@NonNullByDefault
public final class BezierCurveBlockRenderer {
    
    private BezierCurveBlockRenderer() {
    }

    public static void addSectionGeometry(AddSectionGeometryEvent event) {
        var sectionOrigin = event.getSectionOrigin();
        var level = event.getLevel();
        var blockPositions = level.getChunkAt(sectionOrigin).getExistingDataOrNull(PowerToolAttachments.BEZIER_CURVES);
        if (blockPositions == null || blockPositions.isEmpty()) return;
        var renderData = new ArrayList<RenderData>();
        for (var blockPos : blockPositions) {
            var blockEntity = level.getBlockEntity(blockPos);
            if (!(blockEntity instanceof BezierCurveBlockEntity bezierCurveBlockEntity)) continue;
            var data = createRenderData(bezierCurveBlockEntity, sectionOrigin);
            if (data != null) {
                renderData.add(data);
            }
        }
        if (!renderData.isEmpty()) {
            event.addRenderer(context -> render(renderData, context));
        }
    }

    public static void updateChunk(int chunkX, int chunkZ, List<BlockPos> blockPositions) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) return;
        var chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        if (chunk == null) return;
        chunk.setData(PowerToolAttachments.BEZIER_CURVES, List.copyOf(blockPositions));
        minecraft.levelRenderer.setSectionRangeDirty(
                chunkX,
                level.getMinSectionY(),
                chunkZ,
                chunkX,
                level.getMaxSectionY(),
                chunkZ
        );
    }

    @Nullable
    private static RenderData createRenderData(BezierCurveBlockEntity blockEntity, BlockPos sectionOrigin) {
        var model = blockEntity.line;
        if (model == null) return null;
        var bounds = new AABB(
                sectionOrigin.getX() - 2,
                sectionOrigin.getY() - 2,
                sectionOrigin.getZ() - 2,
                sectionOrigin.getX() + 18,
                sectionOrigin.getY() + 18,
                sectionOrigin.getZ() + 18
        );
        var segments = new ArrayList<SegmentData>();
        for (int i = 0; i < model.line.size() - 1; i++) {
            var start = toWorldPosition(model.line.get(i), blockEntity.getBlockPos(), blockEntity.worldCoordinate);
            var end = toWorldPosition(model.line.get(i + 1), blockEntity.getBlockPos(), blockEntity.worldCoordinate);
            var segment = clipSegment(i, start, end, bounds);
            if (segment != null) {
                segments.add(segment);
            }
        }
        if (segments.isEmpty()) return null;
        var texture = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(blockEntity.texture);
        var level = blockEntity.getLevel();
        int packedLight = level == null ? 15728880 : LevelRenderer.getLightCoords(level, blockEntity.getBlockPos());
        return new RenderData(
                model,
                texture,
                blockEntity.getBlockPos(),
                sectionOrigin,
                blockEntity.worldCoordinate,
                blockEntity.sideCount,
                blockEntity.uScale,
                blockEntity.vScale,
                blockEntity.color,
                packedLight,
                segments
        );
    }

    @Nullable
    private static SegmentData clipSegment(int index, Vec3 start, Vec3 end, AABB bounds) {
        double minT = 0;
        double maxT = 1;
        double[] origins = {start.x, start.y, start.z};
        double[] deltas = {end.x - start.x, end.y - start.y, end.z - start.z};
        double[] minimums = {bounds.minX, bounds.minY, bounds.minZ};
        double[] maximums = {bounds.maxX, bounds.maxY, bounds.maxZ};
        for (int axis = 0; axis < origins.length; axis++) {
            double delta = deltas[axis];
            if (Math.abs(delta) < 1.0E-7) {
                if (origins[axis] < minimums[axis] || origins[axis] > maximums[axis]) return null;
                continue;
            }
            double first = (minimums[axis] - origins[axis]) / delta;
            double second = (maximums[axis] - origins[axis]) / delta;
            if (first > second) {
                double temporary = first;
                first = second;
                second = temporary;
            }
            minT = Math.max(minT, first);
            maxT = Math.min(maxT, second);
            if (minT > maxT) return null;
        }
        return new SegmentData(index, (float) minT, (float) maxT);
    }

    private static Vec3 toWorldPosition(Vector3f position, BlockPos blockPos, boolean worldCoordinate) {
        if (worldCoordinate) {
            return new Vec3(position.x, position.y, position.z);
        }
        return new Vec3(
                position.x + blockPos.getX(),
                position.y + blockPos.getY(),
                position.z + blockPos.getZ()
        );
    }

    private static void render(List<RenderData> renderData, AddSectionGeometryEvent.SectionRenderingContext context) {
        var buffer = context.getOrCreateChunkBuffer(ChunkSectionLayer.CUTOUT);
        for (var data : renderData) {
            render(data, buffer);
        }
    }

    @SuppressWarnings("resource")
    private static void render(RenderData data, VertexConsumer buffer) {
        var model = data.model();
        var vertexList = model.vertexAndNormalQuadsList();
        var sideCount = data.sideCount();
        if (vertexList.size() < (model.line.size() - 1) * sideCount * 4) return;
        float uScale = data.uScale() == 0 ? 1 : 1f / data.uScale();
        float vScale = data.vScale() == 0 ? 1 : 1f / data.vScale();
        for (var segment : data.segments()) {
            float baseU = segment.index() * uScale % 1f;
            float startU = baseU + segment.start() * uScale;
            float endU = baseU + segment.end() * uScale;
            for (int side = 0; side < sideCount; side++) {
                float v = side * vScale % 1f;
                int pointer = side * 4 + segment.index() * sideCount * 4;
                var startSide = vertexList.get(pointer);
                var endSide = vertexList.get(pointer + 1);
                var endNextSide = vertexList.get(pointer + 2);
                var startNextSide = vertexList.get(pointer + 3);
                putVertex(buffer, data, startSide, endSide, segment.start(), data.texture().getU(startU), data.texture().getV(v));
                putVertex(buffer, data, startSide, endSide, segment.end(), data.texture().getU(endU), data.texture().getV(v));
                putVertex(buffer, data, startNextSide, endNextSide, segment.end(), data.texture().getU(endU), data.texture().getV(v + vScale));
                putVertex(buffer, data, startNextSide, endNextSide, segment.start(), data.texture().getU(startU), data.texture().getV(v + vScale));
            }
        }
//        for(var i = 0; i < model.vertexAndNormalQuadsList().size()/4; i++){
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4),texture.getU0(),texture.getV0(),-1,packedLight);
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4+1),texture.getU1(),texture.getV0(),-1,packedLight);
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4+2),texture.getU1(),texture.getV1(),-1,packedLight);
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4+3),texture.getU0(),texture.getV1(),-1,packedLight);
//        }
    }

    private static void putVertex(VertexConsumer buffer, RenderData data, Pair<Vector3f, Vector3f> start, Pair<Vector3f, Vector3f> end, float progress, float u, float v) {
        var vertex = start.getFirst().lerp(end.getFirst(), progress, new Vector3f());
        var normal = start.getSecond().lerp(end.getSecond(), progress, new Vector3f());
        if (normal.lengthSquared() > 1.0E-7f) {
            normal.normalize();
        }
        float offsetX = data.worldCoordinate() ? 0 : data.blockPos().getX();
        float offsetY = data.worldCoordinate() ? 0 : data.blockPos().getY();
        float offsetZ = data.worldCoordinate() ? 0 : data.blockPos().getZ();
        buffer.addVertex(
                        vertex.x + offsetX - data.sectionOrigin().getX(),
                        vertex.y + offsetY - data.sectionOrigin().getY(),
                        vertex.z + offsetZ - data.sectionOrigin().getZ()
                )
                .setUv(u, v)
                .setLight(data.packedLight())
                .setColor(data.color())
                .setNormal(normal.x, normal.y, normal.z);
    }

    private record RenderData(
            Line3f model,
            TextureAtlasSprite texture,
            BlockPos blockPos,
            BlockPos sectionOrigin,
            boolean worldCoordinate,
            int sideCount,
            int uScale,
            int vScale,
            int color,
            int packedLight,
            List<SegmentData> segments
    ) {
    }

    private record SegmentData(int index, float start, float end) {
    }
}
