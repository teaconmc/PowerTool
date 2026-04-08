package org.teacon.powertool.client.eyelib.render.sections;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.jspecify.annotations.NonNull;

/**
 * @author Argon4W
 */
public record SectionGeometryBlockEntityRenderDispatcher(
        BlockPos regionOrigin) implements AddSectionGeometryEvent.AdditionalSectionRenderer {
    
    @Override
    public void render(AddSectionGeometryEvent.@NonNull SectionRenderingContext context) {
        BlockPos.betweenClosed(regionOrigin, regionOrigin.offset(15, 15, 15)).forEach(pos -> renderAt(pos, context));
    }
    
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> T cast(BlockEntity o) {
        return (T) o;
    }
    
    public void renderAt(BlockPos pos, AddSectionGeometryEvent.SectionRenderingContext context) {
        BlockEntity blockEntity = context.getRegion().getBlockEntity(pos);
        
        if (blockEntity == null) {
            return;
        }
        
        if (!(Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity) instanceof BlockEntitySectionGeometryRenderer<?> renderer)) {
            return;
        }
        
        context.getPoseStack().pushPose();
        context.getPoseStack().translate(pos.getX() - regionOrigin.getX(), pos.getY() - regionOrigin.getY(), pos.getZ() - regionOrigin.getZ());
        
        int packedLight = LightTexture.pack(context.getRegion().getBrightness(LightLayer.BLOCK, pos), context.getRegion().getBrightness(LightLayer.SKY, pos));
        MultiBufferSource bufferSource = renderType -> new QuadLighterVertexConsumer(context.getOrCreateChunkBuffer(renderType), context, pos);
        
        try {
            renderer.renderSectionGeometry(cast(blockEntity), context, new PoseStack(), pos, regionOrigin, packedLight, bufferSource);
        } catch (ClassCastException ignored) {
        
        }
        
        context.getPoseStack().popPose();
    }
}
