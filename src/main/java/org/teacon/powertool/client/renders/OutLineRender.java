package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.teacon.powertool.item.ExamineHoloGlass;

@EventBusSubscriber
public class OutLineRender {
    
    @SubscribeEvent
    public static void renderBEOutLines(RenderLevelStageEvent event){
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        var levelRenderer = event.getLevelRenderer();
        if (levelRenderer.entityEffect == null) return;
        var tags = ExamineHoloGlass.getOutLinedBlockTags();
        var blocks = ExamineHoloGlass.getOtherLinedBlocks();
        if (tags.isEmpty() && blocks.isEmpty()) return;
        var mc = Minecraft.getInstance();
        var r = mc.options.getEffectiveRenderDistance();
        var level = mc.level;
        if(level == null) return;
        var center = new ChunkPos(event.getCamera().getBlockPosition());
        var sx = center.x-r;
        var sz = center.z-r;
        for (int i = 0; i < r*2+1; i++) {
            for (int j = 0; j < r*2+1; j++) {
                if(Math.abs(j-r)+Math.abs(i-r)>r) continue;
                var chunk = level.getChunk(sx +j, sz +i);
                chunk.getBlockEntities().values()
                        .stream()
                        .filter(be -> be.getBlockState().getTags().anyMatch(tags::contains) || blocks.contains(be.getBlockState().getBlock()) )
                        .forEach(be -> renderOutLine(be.getBlockPos(),event.getPoseStack(),event.getCamera()));
            }
        }
        mc.renderBuffers().outlineBufferSource().endOutlineBatch();
        levelRenderer.entityEffect.process(event.getPartialTick().getGameTimeDeltaTicks());
        mc.getMainRenderTarget().bindWrite(false);
    }
    
    public static void renderOutLine(BlockPos pos, PoseStack poseStack, Camera camera){
        var mc = Minecraft.getInstance();
        if(mc.level == null) return;
        var state = mc.level.getBlockState(pos);
        var outline = RenderType.outline(InventoryMenu.BLOCK_ATLAS);
        var outlineSource = mc.renderBuffers().outlineBufferSource();
        var blockModel = mc.getBlockRenderer().getBlockModel(Blocks.DIRT.defaultBlockState());
        var vec3 = camera.getPosition();
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        poseStack.pushPose();
        poseStack.translate((double)pos.getX() - d0, (double)pos.getY() - d1, (double)pos.getZ() - d2);
        mc.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), outlineSource.getBuffer(outline), state, blockModel,
                0.0F, 0.0F, 0.0F, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, outline);
        poseStack.popPose();
    }
}
