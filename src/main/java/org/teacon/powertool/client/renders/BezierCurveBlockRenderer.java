package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.ChunkPos;
import org.joml.Vector3f;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.BezierCurveBlockEntity;

@NonNullByDefault
public final class BezierCurveBlockRenderer {
    private BezierCurveBlockRenderer() {
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
        int packedLight = level == null
                ? 15728880
                : LevelRenderer.getLightCoords(level, lightPos);
        buffer.addVertex(worldX - chunkPos.getMinBlockX(), worldY, worldZ - chunkPos.getMinBlockZ())
                .setColor(blockEntity.color)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(normal.x, normal.y, normal.z);
    }
}
