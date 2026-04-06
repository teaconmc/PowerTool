package org.teacon.powertool.client.renders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.item.ExamineHoloGlass;

@EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
public class OutLineRender {
    
    
    @SubscribeEvent
    public static void renderBEOutLines(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) return;
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
        for (int i = 0; i < r * 2 + 1; i++) {
            for (int j = 0; j < r * 2 + 1; j++) {
                if (Math.abs(j - r) + Math.abs(i - r) > r) continue;
                var chunk = level.getChunk(sx + j, sz + i);
                chunk.getBlockEntities().values()
                        .stream()
                        .filter(be -> be.getBlockState().tags().anyMatch(tags::contains) || blocks.contains(be.getBlockState().getBlock()))
                        .forEach(be -> {
                            var blockModelResolver = mc.getBlockModelResolver();
                            var state = new BlockModelRenderState();
                            var blockPos = be.getBlockPos();
                            poseStack.pushPose();
                            poseStack.translate(blockPos.getX() - camX, blockPos.getY() - camY, blockPos.getZ() - camZ);
                            blockModelResolver.update(state,be.getBlockState(), BlockDisplayContext.create());
                            state.submitOnlyOutline(poseStack,submitControl,15728880,OverlayTexture.NO_OVERLAY,0);
                            poseStack.popPose();
                        });
            }
        }
    }
}
