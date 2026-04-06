/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.client.renders.holo_sign;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.teacon.powertool.block.entity.BaseHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HolographicSignBlockEntityRenderer implements BlockEntityRenderer<CommonHolographicSignBlockEntity> {
    //private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, -0.2F);
    private final BlockEntityRenderDispatcher dispatcher;
    private final Font font;
    
    public HolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
        this.dispatcher = context.getBlockEntityRenderDispatcher();
    }
    
    @Override
    public void render(CommonHolographicSignBlockEntity theSign, float partialTick, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderInternal(theSign, transform, bufferSource, packedLight, theSign.yRotate, theSign.xRotate);
        if (theSign.bidirectional) {
            renderInternal(theSign, transform, bufferSource, packedLight, (theSign.yRotate + 180) % 360, (360 - theSign.xRotate) % 360);
        }
    }
    
    public void renderInternal(CommonHolographicSignBlockEntity theSign, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int yRotation, int xRotation) {
        transform.pushPose();
        beforeRender(theSign, transform, dispatcher, yRotation, xRotation);
        //VanillaUtils.ClientHandler.renderAxis(bufferSource,transform);
        Matrix4f matrix4f = transform.last().pose();
        int bgColor = getBackgroundColor(theSign);
        var dropShadow = theSign.dropShadow;
        var contents = theSign.renderedContents;
        int yOffset = (-contents.size() * (this.font.lineHeight + 2) + 2) / 2;
        int fontColor = theSign.colorInARGB;
        int[] widths = new int[contents.size()];
        int maxWidth = 0;
        for (int i = 0; i < contents.size(); i++) {
            int w = widths[i] = this.font.width(contents.get(i));
            if (w > maxWidth) {
                maxWidth = w;
            }
        }
        var align = theSign.align;
        for (int i = 0; i < contents.size(); i++) {
            var text = contents.get(i);
            if (text != null && !text.isEmpty()) {
                int xOffset = switch (align) {
                    case LEFT -> -maxWidth / 2;
                    case CENTER -> -widths[i] / 2;
                    case RIGHT -> maxWidth / 2 - widths[i];
                };
                renderText(font, text, xOffset, yOffset, widths[i], fontColor, dropShadow, matrix4f, bufferSource, bgColor, packedLight);
            }
            yOffset += this.font.lineHeight + 2;
        }
        transform.popPose();
    }
    
    public static void beforeRender(BaseHolographicSignBlockEntity theSign, PoseStack transform, BlockEntityRenderDispatcher dispatcher, int yRotation, int xRotation) {
        transform.translate(0.5, 0.5, 0.5);
        if (theSign.lock) {
            transform.mulPose(Axis.YP.rotationDegrees(yRotation));
            transform.mulPose(Axis.XP.rotationDegrees(xRotation));
        } else {
            transform.mulPose(dispatcher.camera.rotation());
            transform.mulPose(Axis.YP.rotationDegrees(180));
        }
        transform.scale(-0.025F * theSign.scale, -0.025F * theSign.scale, -0.25F);
        transform.translate(0.0, 0.0, -theSign.zOffset * 4);
    }
    
    public static int getBackgroundColor(BaseHolographicSignBlockEntity theSign) {
        int bgColor = VanillaUtils.TRANSPARENT;
        if (theSign.renderBackground) bgColor = 0x40000000;
        return bgColor;
    }
    
    @SuppressWarnings("DuplicatedCode")
    public static void renderText(Font font, Component component, float x, float y, int width, int color,
                                  boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer,
                                  int backgroundColor, int packedLightCoords) {
        renderBackground(backgroundColor, packedLightCoords, x, y, width, dropShadow, matrix, buffer);
        font.drawInBatch(component, x, y, color, dropShadow, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, packedLightCoords);
    }
    
    @SuppressWarnings("DuplicatedCode")
    public static void renderText(Font font, String text, float x, float y, int width, int color,
                                  boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer,
                                  int backgroundColor, int packedLightCoords) {
        renderBackground(backgroundColor, packedLightCoords, x, y, width, dropShadow, matrix, buffer);
        font.drawInBatch(text, x, y, color, dropShadow, matrix, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, packedLightCoords);
    }
    
    public static void renderBackground(int backgroundColor, int packedLightCoords, float x, float y, int width,
                                        boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer) {
        if (backgroundColor != 0 && backgroundColor != VanillaUtils.TRANSPARENT) {
            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.textBackground());
            vertexconsumer.addVertex(matrix, x - 1.0F, y - 1.0F, 0.0F).setColor(backgroundColor).setLight(packedLightCoords);
            vertexconsumer.addVertex(matrix, x - 1.0F, y + 9.0F, 0.0F).setColor(backgroundColor).setLight(packedLightCoords);
            vertexconsumer.addVertex(matrix, x + width + 1.0F, y + 9.0F, 0.0F).setColor(backgroundColor).setLight(packedLightCoords);
            vertexconsumer.addVertex(matrix, x + width + 1.0F, y - 1.0F, 0.0F).setColor(backgroundColor).setLight(packedLightCoords);
        }
    }
}
