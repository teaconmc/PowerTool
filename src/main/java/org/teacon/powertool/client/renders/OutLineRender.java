package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.RenderShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.item.ExamineHoloGlass;

@EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
@NonNullByDefault
public class OutLineRender {
    
    private static final int OUTLINE_COLOR = 0xFF55FFFF;
    private static final Identifier OUTLINE_TEXTURE = Identifier.withDefaultNamespace("textures/block/white_concrete.png");
    
    @SubscribeEvent
    public static void renderBEOutLines(RenderLevelStageEvent.AfterSky event) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null || !ExamineHoloGlass.wearingExamineHoloGlass()) return;
        var tags = ExamineHoloGlass.getOutLinedBlockTags();
        var blocks = ExamineHoloGlass.getOtherLinedBlocks();
        if (tags.isEmpty() && blocks.isEmpty()) return;
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        var center = ChunkPos.containing(camera.blockPosition());
        var r = mc.options.getEffectiveRenderDistance();
        var sx = center.x() - r;
        var sz = center.z() - r;
        var submitControl = event.getLevelRenderer().submitNodeStorage;
        var poseStack = event.getPoseStack();
        var cameraPos = camera.position();
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();
        var blockModelResolver = mc.getBlockModelResolver();
        for (int i = 0; i < r * 2 + 1; i++) {
            for (int j = 0; j < r * 2 + 1; j++) {
                if (Math.abs(j - r) + Math.abs(i - r) > r) continue;
                var chunk = level.getChunk(sx + j, sz + i);
                chunk.getBlockEntities().values()
                        .stream()
                        .filter(be -> be.getBlockState().tags().anyMatch(tags::contains) || blocks.contains(be.getBlockState().getBlock()))
                        .forEach(be -> {
                            var blockPos = be.getBlockPos();
                            poseStack.pushPose();
                            poseStack.translate(blockPos.getX() - camX, blockPos.getY() - camY, blockPos.getZ() - camZ);
                            var blockState = be.getBlockState();
                            if (blockState.getRenderShape() == RenderShape.MODEL) {
                                var state = new BlockModelRenderState();
                                blockModelResolver.update(state, blockState, BlockDisplayContext.create());
                                state.submitOnlyOutline(poseStack, submitControl, 15728880, OverlayTexture.NO_OVERLAY, OUTLINE_COLOR);
                            } else {
                                submitControl.submitCustomGeometry(poseStack, RenderTypes.outline(OUTLINE_TEXTURE), OutLineRender::renderBlockCube);
                            }
                            poseStack.popPose();
                        });
            }
        }
    }
    
    @SubscribeEvent
    public static void onFrameGraphSetup(FrameGraphSetupEvent event) {
        if(ExamineHoloGlass.wearingExamineHoloGlass()) event.enableOutlineProcessing();
    }
    
    private static void renderBlockCube(PoseStack.Pose pose, VertexConsumer buffer) {
        addQuad(buffer, pose, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0);
        addQuad(buffer, pose, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, 1);
        addQuad(buffer, pose, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0);
        addQuad(buffer, pose, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1);
        addQuad(buffer, pose, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0);
        addQuad(buffer, pose, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1);
    }
    
    private static void addQuad(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float x4,
            float y4,
            float z4
    ) {
        addVertex(buffer, pose, x1, y1, z1, 0, 0);
        addVertex(buffer, pose, x2, y2, z2, 0, 1);
        addVertex(buffer, pose, x3, y3, z3, 1, 1);
        addVertex(buffer, pose, x4, y4, z4, 1, 0);
    }
    
    private static void addVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(OUTLINE_COLOR);
    }
}
