package org.teacon.powertool.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.teacon.powertool.block.entity.HolographicSignBlockEntity;

public class HolographicSignBlockEntityRenderer implements BlockEntityRenderer<HolographicSignBlockEntity> {
    private final BlockEntityRenderDispatcher dispatcher;
    private final Font font;

    public HolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
        this.dispatcher = context.getBlockEntityRenderDispatcher();
    }

    @Override
    public void render(HolographicSignBlockEntity theSign, float partialTick, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        transform.pushPose();
        transform.translate(0.5, 0.5, 0.5);
        transform.mulPose(this.dispatcher.camera.rotation());
        transform.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = transform.last().pose();
        float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int bgColor = (int)(opacity * 255.0F) << 24;
        int yOffset = -theSign.contents.size() / 2 * this.font.lineHeight;
        for (var text : theSign.contents) {
            float xOffset = (float)(-this.font.width(text) / 2);
            this.font.drawInBatch(text, xOffset, yOffset, 0xFFFFFFFF, false, matrix4f, bufferSource, false, bgColor, packedLight);
            yOffset += this.font.lineHeight + 2;
        }
        transform.popPose();
    }
}
