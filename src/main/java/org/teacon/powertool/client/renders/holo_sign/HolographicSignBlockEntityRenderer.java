/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.client.renders.holo_sign;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.entity.BaseHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class HolographicSignBlockEntityRenderer implements BlockEntityRenderer<CommonHolographicSignBlockEntity, HolographicSignBlockEntityRenderer.HoloSignBEState> {
    
    private Font font;
    
    public HolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    
    }
    
    public static class HoloSignStateBase extends BlockEntityRenderState{
        public boolean bidirectional;
        public boolean dropShadow;
        public boolean renderBackground;
        public boolean lock;
        public int xRotate;
        public int yRotate;
        public int colorInARGB;
        public float zOffset;
        public float scale;
        public BaseHolographicSignBlockEntity.Align align;
        
        public void extractState(BaseHolographicSignBlockEntity be){
            this.bidirectional = be.bidirectional;
            this.xRotate = be.xRotate;
            this.yRotate = be.yRotate;
            this.zOffset = be.zOffset;
            this.dropShadow = be.dropShadow;
            this.renderBackground = be.renderBackground;
            this.colorInARGB = be.colorInARGB;
            this.align = be.align;
            this.lock = be.lock;
            this.scale = be.scale;
        }
    }
    
    public static class HoloSignBEState extends HoloSignStateBase {
        public List<String> contents;
    }
    
    @Override
    public HoloSignBEState createRenderState() {
        return new HoloSignBEState();
    }
    
    @Override
    public void extractRenderState(CommonHolographicSignBlockEntity be, HoloSignBEState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        this.font = Minecraft.getInstance().font;
        state.extractState(be);
        state.contents = be.renderedContents;
    }
    
    
    @Override
    public void submit(HoloSignBEState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        renderInternal(state, poseStack, submitNodeCollector, state.lightCoords, state.yRotate, state.xRotate, camera);
        if (state.bidirectional) {
            renderInternal(state, poseStack, submitNodeCollector, state.lightCoords, (state.yRotate + 180) % 360, (360 - state.xRotate) % 360, camera);
        }
    }
    
    public void renderInternal(HoloSignBEState theSign, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int yRotation, int xRotation,CameraRenderState camera) {
        poseStack.pushPose();
        beforeRender(theSign, poseStack, camera, yRotation, xRotation);
        int bgColor = getBackgroundColor(theSign);
        var dropShadow = theSign.dropShadow;
        var contents = theSign.contents;
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
                renderText(poseStack,nodeCollector,Component.literal(text),dropShadow,fontColor,bgColor,packedLight,xOffset,yOffset,widths[i]);
            }
            yOffset += this.font.lineHeight + 2;
        }
        poseStack.popPose();
    }
    
    public static void beforeRender(HoloSignStateBase theSign, PoseStack transform, CameraRenderState camera, int yRotation, int xRotation) {
        transform.translate(0.5, 0.5, 0.5);
        if (theSign.lock) {
            transform.mulPose(Axis.YP.rotationDegrees(yRotation));
            transform.mulPose(Axis.XP.rotationDegrees(xRotation));
        } else {
            transform.mulPose(camera.orientation);
            transform.mulPose(Axis.YP.rotationDegrees(180));
        }
        transform.scale(-0.025F * theSign.scale, -0.025F * theSign.scale, -0.25F);
        transform.translate(0.0, 0.0, -theSign.zOffset * 4);
    }
    
    public static int getBackgroundColor(HoloSignStateBase theSign) {
        int bgColor = VanillaUtils.TRANSPARENT;
        if (theSign.renderBackground) bgColor = 0x40000000;
        return bgColor;
    }
    
    public static void renderText(PoseStack poseStack, SubmitNodeCollector nodeCollector,Component text,boolean dropShadow,int color, int backgroundColor, int packedLightCoords, float x, float y, int width){
        renderBackground(poseStack,nodeCollector,backgroundColor,packedLightCoords,x,y,width);
        nodeCollector.submitText(poseStack,x,y,text.getVisualOrderText(),dropShadow, Font.DisplayMode.POLYGON_OFFSET,packedLightCoords,color,0,-1);
    }

    
    public static void renderBackground(PoseStack poseStack, SubmitNodeCollector nodeCollector, int backgroundColor, int packedLightCoords, float x, float y, int width) {
        if (backgroundColor != 0 && backgroundColor != VanillaUtils.TRANSPARENT) {
            nodeCollector.submitCustomGeometry(poseStack, RenderTypes.textBackground(), (pose, buffer) -> {
                buffer.addVertex(pose, x - 1.0F, y - 1.0F, 0.0F).setColor(backgroundColor).setLight(packedLightCoords);
                buffer.addVertex(pose, x - 1.0F, y + 9.0F, 0.0F).setColor(backgroundColor).setLight(packedLightCoords);
                buffer.addVertex(pose, x + width + 1.0F, y + 9.0F, 0.0F).setColor(backgroundColor).setLight(packedLightCoords);
                buffer.addVertex(pose, x + width + 1.0F, y - 1.0F, 0.0F).setColor(backgroundColor).setLight(packedLightCoords);
            });

        }
    }
}
