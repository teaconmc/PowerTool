package org.teacon.powertool.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.teacon.powertool.block.entity.ItemSupplierBlockEntity;

public class ItemSupplierBlockEntityRenderer implements BlockEntityRenderer<ItemSupplierBlockEntity> {

    private static final Direction[] HORIZONTAL = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

    public ItemSupplierBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // No-op
    }

    @Override
    public void render(ItemSupplierBlockEntity theBe, float partialTick, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack theItem = theBe.theItem;
        if (!theItem.isEmpty()) {
            var mc =  Minecraft.getInstance();
            var level = mc.level;
            float rotation = 0F;
            if (level != null) {
                rotation = level.getLevelData().getGameTime() + partialTick;
            }
            transform.pushPose();
            transform.translate(0.5, 0.5, 0.5);
            transform.scale(0.625F, 0.625F, 0.625F);
            transform.mulPose(Vector3f.YP.rotationDegrees(rotation));
            mc.getItemRenderer().renderStatic(theBe.theItem, ItemTransforms.TransformType.FIXED, LightTexture.FULL_BRIGHT, packedOverlay, transform, bufferSource, (int) theBe.getBlockPos().asLong());
            transform.popPose();
        }

    }
}
