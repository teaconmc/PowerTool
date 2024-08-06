package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LinkHolographicSignBlockEntityRenderer implements BlockEntityRenderer<LinkHolographicSignBlockEntity> {
    
    private static final Style LINK_STYLE = Style.EMPTY.withUnderlined(true);
    private final BlockEntityRenderDispatcher dispatcher;
    private final Font font;
    
    public LinkHolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
        this.dispatcher = context.getBlockEntityRenderDispatcher();
    }
    
    @Override
    public void render(LinkHolographicSignBlockEntity theSign, float partialTick, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderInternal(theSign,transform,bufferSource,packedLight,theSign.rotate);
        if(theSign.bidirectional){
            renderInternal(theSign,transform,bufferSource,packedLight,(theSign.rotate+180)%360);
        }
    }
    
    public void renderInternal(LinkHolographicSignBlockEntity theSign, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int rotatedDegree){
        transform.pushPose();
        HolographicSignBlockEntityRenderer.beforeRender(theSign,transform,dispatcher,rotatedDegree);
        Matrix4f matrix4f = transform.last().pose();
        var text = theSign.displayContent.getString();
        text = text.startsWith("🌐") ? text : "🌐"+ text;
        int bgColor = theSign.bgColorInARGB;
        int yOffset = (int) -(0.5 * this.font.lineHeight);
        int fontColor = theSign.colorInARGB;
        int w = this.font.width(text);
        //var align = theSign.align;
        //todo 就一行需要不同的对齐方式吗
//        int xOffset = switch (align) {
//            case LEFT -> 8;
//            case CENTER -> -w / 2;
//            case RIGHT -> 8 - w / 2;
//        };
        // FIXME Implement all 3 different shadow types
        this.font.drawInBatch(Component.literal(text).withStyle(LINK_STYLE), (float) -w / 2, yOffset, fontColor, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, bgColor, packedLight);
    
        
        
        transform.popPose();
    }
}
