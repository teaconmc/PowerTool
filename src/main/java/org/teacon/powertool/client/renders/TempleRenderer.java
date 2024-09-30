package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.TempleBlock;
import org.teacon.powertool.block.entity.TempleBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TempleRenderer implements BlockEntityRenderer<TempleBlockEntity> {
    
    public TempleRenderer(BlockEntityRendererProvider.Context context) {
        // No-op
    }
    
    @Override
    public void render(TempleBlockEntity te, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var level = te.getLevel();
        var item = te.theItem;
        if(level == null || item.isEmpty()) return;
        var bs = level.getBlockState(te.getBlockPos());
        if(!bs.is(PowerToolBlocks.TEMPLE.get())) return;
        var facing = bs.getValue(TempleBlock.HORIZONTAL_FACING);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.7, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(((facing.get2DDataValue()+2)%4)*90));
        poseStack.translate(0, 0, -0.1875);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, packedOverlay, poseStack, bufferSource, level, (int) te.getBlockPos().asLong());
        poseStack.popPose();
    }
}
