package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.ChunkPos;
import org.teacon.powertool.client.eyelib.render.sections.BlockEntitySectionGeometryRenderer;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.joml.Vector3f;
import org.teacon.powertool.block.entity.BezierCurveBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BezierCurveBlockRenderer implements BlockEntityRenderer<BezierCurveBlockEntity>, BlockEntitySectionGeometryRenderer<BezierCurveBlockEntity> {
    
    public BezierCurveBlockRenderer(BlockEntityRendererProvider.Context ignore) {}
    
    @Override
    public void render(BezierCurveBlockEntity te, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    
    
    }
    
    @Override
    public AABB getRenderBoundingBox(BezierCurveBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
    
    @Override
    public void renderSectionGeometry(BezierCurveBlockEntity te, AddSectionGeometryEvent.SectionRenderingContext context, PoseStack poseStack, BlockPos pos, BlockPos regionOrigin, int packedLight, MultiBufferSource bufferSource) {
        var model = te.line;
        if(model == null) return;
        var line = model.line;
        var clampMode = te.clampMode;
        var steps = te.steps;
        var sideCount = te.sideCount;
        var vertexList = model.vertexAndNormalQuadsList();
        var level = Minecraft.getInstance().level;
        var selfPos = te.getBlockPos();
        var useWorldCoordinate = te.worldCoordinate;
        BlockPos centerPos = null;
        if(level != null) centerPos = level.getChunkAt(selfPos).getPos().getMiddleBlockPosition(0);
        if(vertexList.size() < (steps-1)*sideCount*4) return;
        poseStack = context.getPoseStack();
        poseStack.pushPose();
        if(useWorldCoordinate) poseStack.translate(-selfPos.getX(), -selfPos.getY(), -selfPos.getZ());
        @SuppressWarnings("deprecation")
        var texture = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(te.texture);
        var pose = poseStack.last();
        var buffer = bufferSource.getBuffer(RenderType.CUTOUT);
        var uScale = 1f/te.uScale;
        var vScale = 1f/te.vScale;
        var color = te.color;
        var u = 0f;
        var v = 0f;
        for(var i = 0; i < steps-1; ++i) {
            v = 0f;
            if(centerPos != null && clampMode && !insideRenderChunk(centerPos,line.get(i), useWorldCoordinate ? 0 : selfPos.getX(), useWorldCoordinate ? 0 : selfPos.getZ())) continue;
            for(var j = 0; j < sideCount; ++j) {
                var ptr = j*4+i*sideCount*4;
                putVertex(buffer,pose,vertexList.get(ptr),texture.getU(u),texture.getV(v), color, packedLight);
                putVertex(buffer,pose,vertexList.get(ptr+1),texture.getU(u+uScale),texture.getV(v), color, packedLight);
                putVertex(buffer,pose,vertexList.get(ptr+2),texture.getU(u+uScale),texture.getV(v+vScale), color, packedLight);
                putVertex(buffer,pose,vertexList.get(ptr+3),texture.getU(u),texture.getV(v+vScale), color, packedLight);
                v = (v+vScale)%1f;
            }
            u = (u+uScale)%1f;
        }
//        for(var i = 0; i < model.vertexAndNormalQuadsList().size()/4; i++){
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4),texture.getU0(),texture.getV0(),-1,packedLight);
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4+1),texture.getU1(),texture.getV0(),-1,packedLight);
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4+2),texture.getU1(),texture.getV1(),-1,packedLight);
//            putVertex(buffer,pose,model.vertexAndNormalQuadsList().get(i*4+3),texture.getU0(),texture.getV1(),-1,packedLight);
//        }
        poseStack.popPose();
    }
    
    public static boolean insideRenderChunk(BlockPos chunkCenter,Vector3f renderPos,int offsetX,int offsetZ){
        return Math.abs(chunkCenter.getX()-renderPos.x-offsetX) < 10f && Math.abs(chunkCenter.getZ()-renderPos.z-offsetZ) < 10f;
    }
    
    public static void putVertex(VertexConsumer buffer, PoseStack.Pose pose, Pair<Vector3f,Vector3f> vertexAndNormal, float u, float v, int color, int light){
        var vertex = vertexAndNormal.getFirst();
        var normal = vertexAndNormal.getSecond();
        buffer.addVertex(pose,vertex.x, vertex.y, vertex.z).setUv(u,v).setLight(light).setColor(color).setNormal(pose,normal.x, normal.y, normal.z);
    }
    
}
