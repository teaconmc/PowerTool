package org.teacon.powertool.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.teacon.powertool.block.entity.ItemDisplayBlockEntity;

public class ItemDisplayBlockEntityRenderer implements BlockEntityRenderer<ItemDisplayBlockEntity> {

    private final ItemRenderer itemRenderer;
    public ItemDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }
    @Override
    public void render(ItemDisplayBlockEntity theBE, float partialTick, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        transform.pushPose();
        transform.translate(0.5F, 0.5F, 0.5F);
        var state = theBE.getBlockState();
        var direction = state.getValue(BlockStateProperties.FACING);
        transform.translate(direction.getStepX() * -0.4375F, direction.getStepY() * -0.4375F, direction.getStepZ() * -0.4375F);
        transform.scale(0.5F, 0.5F, 0.5F);
        switch (direction) {
            case DOWN -> transform.mulPose(Axis.XN.rotationDegrees(90));
            case UP -> transform.mulPose(Axis.XN.rotationDegrees(270));
            case EAST -> transform.mulPose(Axis.YP.rotationDegrees(270));
            case SOUTH -> transform.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> transform.mulPose(Axis.YP.rotationDegrees(90));
        }
        this.itemRenderer.renderStatic(theBE.itemToDisplay, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                transform, bufferSource, theBE.getLevel(), 0);
        transform.popPose();

    }
}
