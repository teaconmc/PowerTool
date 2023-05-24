package org.teacon.powertool.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.teacon.powertool.block.entity.ItemSupplierBlockEntity;

public class ItemSupplierBlockEntityRenderer implements BlockEntityRenderer<ItemSupplierBlockEntity> {

    public ItemSupplierBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // No-op
    }

    @Override
    public void render(ItemSupplierBlockEntity theBe, float partialTick, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack theItem = theBe.theItem;
        if (!theItem.isEmpty()) {
            float rotation = ClientEvents.tickCount + partialTick;
            transform.pushPose();
            transform.translate(0.5, 0.5, 0.5);
            transform.scale(0.625F, 0.625F, 0.625F);
            transform.mulPose(Axis.YP.rotationDegrees(rotation));
            Minecraft.getInstance().getItemRenderer().renderStatic(theBe.theItem, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, packedOverlay, transform, bufferSource, theBe.getLevel (), (int) theBe.getBlockPos().asLong());
            transform.popPose();
        }

    }
}
