package org.teacon.powertool.client.renders.holo_sign;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LinkHolographicSignBlockEntityRenderer implements BlockEntityRenderer<LinkHolographicSignBlockEntity, LinkHolographicSignBlockEntityRenderer.LinkSignBEState> {
    
    private static final Style LINK_STYLE = Style.EMPTY.withUnderlined(true);
    private Font font;
    
    public LinkHolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    
    }
    
    public static class LinkSignBEState extends HolographicSignBlockEntityRenderer.HoloSignStateBase{
        public Component displayContent;
    }
    
    @Override
    public LinkSignBEState createRenderState() {
        return new LinkSignBEState();
    }
    
    @Override
    public void extractRenderState(LinkHolographicSignBlockEntity be, LinkSignBEState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        this.font = Minecraft.getInstance().font;
        state.extractState(be);
        state.displayContent = be.displayContent;
    }
    
    @Override
    public void submit(LinkSignBEState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        renderInternal(state, poseStack, submitNodeCollector, state.lightCoords, state.yRotate, state.xRotate, camera);
        if (state.bidirectional) {
            renderInternal(state, poseStack, submitNodeCollector, state.lightCoords, (state.yRotate + 180) % 360, (360 - state.xRotate) % 360, camera);
        }
    }
    
    public void renderInternal(LinkSignBEState theSign, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int yRotation, int xRotation, CameraRenderState camera) {
        poseStack.pushPose();
        HolographicSignBlockEntityRenderer.beforeRender(theSign, poseStack, camera, yRotation, xRotation);
        var text = theSign.displayContent.getString();
        text = text.startsWith("🌐") ? text : "🌐" + text;
        var component = Component.literal(text).withStyle(LINK_STYLE);
        int bgColor = HolographicSignBlockEntityRenderer.getBackgroundColor(theSign);
        int yOffset = (int) -(0.5 * this.font.lineHeight);
        int w = this.font.width(component);
        HolographicSignBlockEntityRenderer.renderText(poseStack,nodeCollector,component, theSign.dropShadow, theSign.colorInARGB , bgColor, packedLight,(float) -w / 2, yOffset, w);
        poseStack.popPose();
    }
}
