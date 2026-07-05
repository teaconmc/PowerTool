package org.teacon.powertool.client.renders.holo_sign;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@NonNullByDefault
public class RawJsonHolographicSignBlockEntityRenderer implements BlockEntityRenderer<RawJsonHolographicSignBlockEntity, RawJsonHolographicSignBlockEntityRenderer.RawJsonSignBEState> {
    
    private  Font font;
    
    public RawJsonHolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    
    }
    
    public static class RawJsonSignBEState extends HolographicSignBlockEntityRenderer.HoloSignStateBase{
        public List<Component> forRender;
    }
    
    @Override
    public RawJsonSignBEState createRenderState() {
        return new RawJsonSignBEState();
    }
    
    @Override
    public void extractRenderState(RawJsonHolographicSignBlockEntity be, RawJsonSignBEState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        this.font = Minecraft.getInstance().font;
        state.extractState(be);
        state.forRender = be.forRender;
    }
    
    @Override
    public void submit(RawJsonSignBEState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        var renderHoverText = Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getBlockPos().equals(state.blockPos);
        renderInternal(state, poseStack, submitNodeCollector, state.lightCoords, state.yRotate, state.xRotate, camera, renderHoverText);
        if (state.bidirectional) {
            renderInternal(state, poseStack, submitNodeCollector, state.lightCoords, (state.yRotate + 180) % 360, (360 - state.xRotate) % 360, camera, renderHoverText);
        }
    }
    
    public void renderComponent(Component component, float x, float y, PoseStack poseStack, SubmitNodeCollector nodeCollector, boolean dropShadow, int packedLight, int fontColorDefault, int bgColor) {
        if (component.equals(Component.empty()) || component.getString().isEmpty()) return;
        var textColor = component.getStyle().getColor();
        int fontColor = textColor == null ? fontColorDefault : textColor.getValue();
        int w = this.font.width(component);
        HolographicSignBlockEntityRenderer.renderText(poseStack,nodeCollector,component,dropShadow,fontColor,bgColor,packedLight,x - (float) w / 2, y, w);
    }
    
    //todo 应用对齐方式
    public float renderComponentList(List<Component> components, float x, float y, PoseStack transform, SubmitNodeCollector nodeCollector, boolean dropShadow, int packedLight, int fontColorDefault, int bgColor, boolean renderHoverText) {
        var yr = y;
        for (var component : components) {
            renderComponent(component, x, y, transform, nodeCollector, dropShadow, packedLight, fontColorDefault, bgColor);
            y += this.font.lineHeight + 1;
            if (renderHoverText) {
                y += renderHoverText(component, x, y, transform, nodeCollector, dropShadow, packedLight, fontColorDefault, bgColor);
            }
        }
        return y - yr;
    }
    
    public float renderHoverText(Component component, float x, float y, PoseStack transform, SubmitNodeCollector nodeCollector, boolean dropShadow, int packedLight,int fontColorDefault, int bgColor) {
        var yr = y;
        var hoverEvent = component.getStyle().getHoverEvent();
        if (hoverEvent != null) {
            if (hoverEvent instanceof HoverEvent.ShowText(Component text)) {
                renderComponent(text, x, y, transform, nodeCollector, dropShadow, packedLight, fontColorDefault, bgColor);
                y += this.font.lineHeight + 1;
            }
            if (hoverEvent instanceof HoverEvent.ShowEntity(HoverEvent.EntityTooltipInfo entity)) {
                y += renderComponentList(entity.getTooltipLines(), x, y, transform, nodeCollector, dropShadow, packedLight, fontColorDefault, bgColor, true);
            }
            if (hoverEvent instanceof HoverEvent.ShowItem(ItemStackTemplate item)) {
                y += renderComponentList(Screen.getTooltipFromItem(Minecraft.getInstance(), item.create()), x, y, transform, nodeCollector, dropShadow, packedLight, fontColorDefault, bgColor, true);
            }
        }
        return y - yr;
    }
    
    public void renderInternal(RawJsonSignBEState theSign, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, int yRotation, int xRotation, CameraRenderState camera, boolean renderHoverText) {
        poseStack.pushPose();
        HolographicSignBlockEntityRenderer.beforeRender(theSign, poseStack, camera, yRotation, xRotation);
        int yOffset = (int) -(0.5 * this.font.lineHeight);
        renderComponentList(theSign.forRender, 0, yOffset, poseStack, nodeCollector, theSign.dropShadow, packedLight, theSign.colorInARGB, HolographicSignBlockEntityRenderer.getBackgroundColor(theSign), renderHoverText);
        poseStack.popPose();
    }
    
    @Override
    public AABB getRenderBoundingBox(RawJsonHolographicSignBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(8,8,8);
    }
}
