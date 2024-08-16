package org.teacon.powertool.client.renders.holo_sign;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class RawJsonHolographicSignBlockEntityRenderer implements BlockEntityRenderer<RawJsonHolographicSignBlockEntity> {
    
    private final BlockEntityRenderDispatcher dispatcher;
    private final Font font;
    
    public RawJsonHolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
        this.dispatcher = context.getBlockEntityRenderDispatcher();
    }
    
    @Override
    public void render(RawJsonHolographicSignBlockEntity theSign, float partialTick, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var renderHoverText = Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getBlockPos().equals(theSign.getBlockPos());
        renderInternal(theSign, transform, bufferSource, packedLight, theSign.rotate,renderHoverText);
        if (theSign.bidirectional) {
            renderInternal(theSign, transform, bufferSource, packedLight, (theSign.rotate + 180) % 360,renderHoverText);
        }
    }
    
    public void renderComponent(Component component,float x,float y,PoseStack transform,MultiBufferSource bufferSource, int packedLight,int fontColorDefault,int bgColor) {
        if(component.equals(Component.empty()) || component.getString().isEmpty()) return;
        var textColor = component.getStyle().getColor();
        int fontColor = textColor == null ? fontColorDefault : textColor.getValue();
        int w = this.font.width(component);
        // FIXME Implement all 3 different shadow types
        this.font.drawInBatch(component,x- (float) w /2,y,fontColor,false,transform.last().pose(),bufferSource, Font.DisplayMode.NORMAL,bgColor,packedLight);
    }
    
    //todo 应用对齐方式
    public void renderComponentList(List<Component> components, float x, float y, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int fontColorDefault, int bgColor) {
        for (int i = 0; i < components.size(); i++) {
            renderComponent(components.get(i),x,y+this.font.lineHeight*i,transform,bufferSource,packedLight,fontColorDefault,bgColor);
        }
    }
    
    public void renderInternal(RawJsonHolographicSignBlockEntity theSign, PoseStack transform, MultiBufferSource bufferSource, int packedLight, int rotatedDegree,boolean renderHoverText) {
        transform.pushPose();
        HolographicSignBlockEntityRenderer.beforeRender(theSign, transform, dispatcher, rotatedDegree);
        int bgColor = theSign.bgColorInARGB;
        int yOffset = (int) -(0.5 * this.font.lineHeight);
        renderComponent(theSign.forRender,0,yOffset,transform,bufferSource,packedLight,theSign.colorInARGB,theSign.bgColorInARGB);
        yOffset+=2;
        if(renderHoverText) {
            var hoverEvent = theSign.forRender.getStyle().getHoverEvent();
            if (hoverEvent != null){
                var action = hoverEvent.getAction();
                if(action == HoverEvent.Action.SHOW_TEXT){
                    var text = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
                    if(text != null){
                        renderComponent(text,0,yOffset+this.font.lineHeight,transform,bufferSource,packedLight,bgColor,bgColor);
                    }
                }
                if(action == HoverEvent.Action.SHOW_ENTITY){
                    var entity_info = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY);
                    if(entity_info != null){
                        renderComponentList(entity_info.getTooltipLines(),0,yOffset+this.font.lineHeight,transform,bufferSource,packedLight,bgColor,bgColor);
                    }
                }
                if(action == HoverEvent.Action.SHOW_ITEM){
                    var item_info = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
                    if(item_info != null){
                        renderComponentList(Screen.getTooltipFromItem(Minecraft.getInstance(),item_info.getItemStack()),0,yOffset+this.font.lineHeight,transform,bufferSource,packedLight,bgColor,bgColor);
                    }
                }
            }
        }
        transform.popPose();
    }
}
