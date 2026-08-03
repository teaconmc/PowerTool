package org.teacon.powertool.client.renders.holo_sign;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.BBCodeHolographicSignBlockEntity;

import java.util.List;

@NonNullByDefault
public class BBCodeHolographicSignBlockEntityRenderer implements BlockEntityRenderer<BBCodeHolographicSignBlockEntity, BBCodeHolographicSignBlockEntityRenderer.BBCSignBEState> {

    private Font font;

    public BBCodeHolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static class BBCSignBEState extends HolographicSignBlockEntityRenderer.HoloSignStateBase {
        public List<BBCodeHolographicSignBlockEntity.ParsedLine> parsedLines;
        public float defaultScale;
        public int defaultColor;
        public float defaultLineHeight;
        public float minWidth;
    }

    @Override
    public BBCSignBEState createRenderState() {
        return new BBCSignBEState();
    }

    @Override
    public void extractRenderState(BBCodeHolographicSignBlockEntity be, BBCSignBEState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        this.font = Minecraft.getInstance().font;
        state.extractState(be);
        state.parsedLines = be.parsedLines;
        state.defaultScale = be.defaultScale;
        state.defaultColor = be.defaultColor;
        state.defaultLineHeight = be.defaultLineHeight;
        state.minWidth = be.minWidth;
    }

    @Override
    public void submit(BBCSignBEState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        renderInternal(state, poseStack, submitNodeCollector, state.lightCoords, false, camera);
        if (state.bidirectional) {
            renderInternal(state, poseStack, submitNodeCollector, state.lightCoords, true, camera);
        }
    }

    public void renderInternal(BBCSignBEState theSign, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, boolean backFace, CameraRenderState camera) {
        poseStack.pushPose();
        HolographicSignBlockEntityRenderer.beforeRender(theSign, poseStack, camera, backFace);

        var parsedLines = theSign.parsedLines;
        if (parsedLines.isEmpty()) {
            poseStack.popPose();
            return;
        }

        int bgColor = HolographicSignBlockEntityRenderer.getBackgroundColor(theSign);

        // Calculate total height and line information
        float totalHeight = 0;
        int[] lineWidths = new int[parsedLines.size()];
        float[] lineHeightMultipliers = new float[parsedLines.size()];
        float[] maxScalesPerLine = new float[parsedLines.size()];

        for (int i = 0; i < parsedLines.size(); i++) {
            var line = parsedLines.get(i);
            int maxWidth = 0;
            float maxScale = theSign.defaultScale;
            float lineHeightMult = line.lineLineHeight != null ? line.lineLineHeight : theSign.defaultLineHeight;

            for (var segment : line.segments) {
                float segmentScale = segment.size() != null ? segment.size() : theSign.defaultScale;
                int segmentWidth = (int) (this.font.width(segment.text()) * segmentScale);
                if (segmentWidth > maxWidth) {
                    maxWidth = segmentWidth;
                }
                if (segmentScale > maxScale) {
                    maxScale = segmentScale;
                }
            }

            // Apply minimum width (convert from blocks to pixels at scale 1.0)
            // At scale 1.0, 1 block = 40 pixels (0.025 scale factor)
            float minPixelWidth = theSign.minWidth * 40.0F;
            if (maxWidth < minPixelWidth) {
                maxWidth = (int) minPixelWidth;
            }

            lineWidths[i] = maxWidth;
            lineHeightMultipliers[i] = lineHeightMult;
            maxScalesPerLine[i] = maxScale;

            // Calculate line height: (font height + 2) * lineHeightMultiplier * maxScaleInLine
            float lineHeight = (this.font.lineHeight + 2) * lineHeightMult * maxScale;
            totalHeight += lineHeight;
        }

        // Starting Y position (centered vertically)
        float currentY = -totalHeight / 2.0F;

        for (int i = 0; i < parsedLines.size(); i++) {
            var line = parsedLines.get(i);
            var lineAlign = line.lineAlign;
            int lineWidth = lineWidths[i];
            float lineHeight = (this.font.lineHeight + 2) * lineHeightMultipliers[i] * maxScalesPerLine[i];

            // Calculate starting X based on alignment
            float segmentStartX = switch (lineAlign) {
                case LEFT -> -lineWidth / 2.0F;
                case CENTER -> 0;
                case RIGHT -> lineWidth / 2.0F;
            };

            float currentX = segmentStartX;

            for (var segment : line.segments) {
                if (segment.text() != null && !segment.text().isEmpty()) {
                    float segmentScale = segment.size() != null ? segment.size() : theSign.defaultScale;
                    int segmentColor = segment.color() != null ? segment.color() : theSign.defaultColor;
                    // Use segment background color if specified, otherwise use global background
                    int segmentBgColor = segment.backgroundColor() != null ? segment.backgroundColor() : bgColor;

                    // Calculate segment width with scale
                    int segmentWidth = (int) (this.font.width(segment.text()) * segmentScale);

                    // Calculate X position for this segment based on line alignment
                    // For continuous text segments, we need to handle alignment properly:
                    // LEFT: segments start from segmentStartX and extend right
                    // CENTER: segments are centered around their cumulative position
                    // RIGHT: segments end at segmentStartX and extend left
                    float x = getX(lineAlign, currentX);

                    // For RIGHT align, currentX moves left (decreases) as we add segments
                    // For LEFT and CENTER, currentX moves right (increases)

                    // Create styled component for rendering
                    Style style = Style.EMPTY
                            .withColor(net.minecraft.network.chat.TextColor.fromRgb(segmentColor & 0xFFFFFF))
                            .withBold(segment.bold() != null ? segment.bold() : theSign.dropShadow)
                            .withUnderlined(segment.underline() != null ? segment.underline() : false)
                            .withItalic(segment.italic() != null ? segment.italic() : false);

                    Component styledComponent = Component.literal(segment.text()).setStyle(style);

                    // Render the segment with its specific scale and background color
                    // For CENTER align, x represents the center of the segment
                    // For LEFT/RIGHT align, x represents the left edge
                    boolean isCenterAlign = lineAlign == BBCodeHolographicSignBlockEntity.TextAlign.CENTER;
                    renderSegmentWithScale(poseStack, nodeCollector, styledComponent, theSign.dropShadow,
                            segmentColor, segmentBgColor, packedLight, x, currentY, segmentScale, segmentWidth, isCenterAlign);

                    // Update currentX for next segment
                    if (lineAlign == BBCodeHolographicSignBlockEntity.TextAlign.LEFT) {
                        currentX += segmentWidth;
                    } else if (lineAlign == BBCodeHolographicSignBlockEntity.TextAlign.CENTER) {
                        // Move center position by half of current segment + half of next segment's width
                        // But since we don't know next segment's width yet, we move by full current width
                        // The next segment's center will be at current position + currentWidth/2 + nextWidth/2
                        // For simplicity: currentX tracks the right edge of current segment
                        currentX += segmentWidth;
                    } else { // RIGHT
                        currentX -= segmentWidth;
                    }
                    segmentStartX = currentX;
                }
            }

            // Move to next line
            currentY += lineHeight;
        }

        poseStack.popPose();
    }

    private static float getX(BBCodeHolographicSignBlockEntity.TextAlign lineAlign, float currentX) {
        float x;
        if (lineAlign == BBCodeHolographicSignBlockEntity.TextAlign.LEFT) {
            // Left align: x is the left edge of the segment
            x = currentX;
        } else if (lineAlign == BBCodeHolographicSignBlockEntity.TextAlign.CENTER) {
            // Center align: x is the center of the segment
            // currentX is the center position where this segment should be placed
            // But we want to render from the segment's perspective, so we need to offset
            x = currentX;
        } else { // RIGHT
            // Right align: x is the right edge of the segment
            x = currentX;
        }
        return x;
    }

    /**
     * Render a text segment with a specific scale.
     * We need to apply the scale transformation for each segment individually.
     *
     * @param isCenterAlign If true, x represents the center of the segment; otherwise x is the left edge
     */
    private void renderSegmentWithScale(PoseStack poseStack, SubmitNodeCollector nodeCollector,
            Component textComponent, boolean dropShadow, int color, int backgroundColor, int packedLight,
            float x, float y, float scale, int width, boolean isCenterAlign) {

        poseStack.pushPose();

        // Apply scale for this segment
        poseStack.translate(x, y, 0);
        poseStack.scale(scale, scale, 1.0F);
        poseStack.translate(-x, -y, 0);

        // Calculate render position based on alignment
        float renderX = x;
        if (isCenterAlign) {
            // For center align, x is the center position, so we offset by half width
            renderX = x - (float) width / 2.0F / scale;
        }

        // Render background if enabled
        if (backgroundColor != 0 && backgroundColor != org.teacon.powertool.utils.VanillaUtils.TRANSPARENT) {
            // Note: background is drawn at the scaled position
            HolographicSignBlockEntityRenderer.renderBackground(poseStack, nodeCollector, backgroundColor, packedLight,
                    renderX - 1.0F / scale, y - 1.0F / scale, (int) ((float) width / scale));
        }

        // Render text with styling (bold, underline, italic handled by the component's style)
        nodeCollector.submitText(poseStack, renderX, y, textComponent.getVisualOrderText(),
                dropShadow, Font.DisplayMode.POLYGON_OFFSET, packedLight, color, 0, 0);

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(BBCodeHolographicSignBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(8, 8, 8);
    }
}
