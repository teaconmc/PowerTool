package org.teacon.powertool.client.renders.holo_sign;

import com.mojang.blaze3d.vertex.PoseStack;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.BBCodeHolographicSignBlockEntity;

import java.util.ArrayList;
import java.util.List;

@NonNullByDefault
public class BBCodeHolographicSignBlockEntityRenderer implements BlockEntityRenderer<BBCodeHolographicSignBlockEntity, BBCodeHolographicSignBlockEntityRenderer.BBCSignBEState> {

    private Font font;

    public BBCodeHolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    /**
     * Cached render data for a single text segment.
     */
    public static class CachedSegment {
        public final Component component;
        public final float scale;
        public final int color;
        public final int backgroundColor;
        public final int width;

        public CachedSegment(Component component, float scale, int color, int backgroundColor, int width) {
            this.component = component;
            this.scale = scale;
            this.color = color;
            this.backgroundColor = backgroundColor;
            this.width = width;
        }
    }

    /**
     * Cached render data for a single line.
     */
    public static class CachedLine {
        public final List<CachedSegment> segments;
        public final BBCodeHolographicSignBlockEntity.TextAlign align;
        public final int lineWidth;
        public final float lineHeight;
        public final float maxScale;

        public CachedLine(List<CachedSegment> segments, BBCodeHolographicSignBlockEntity.TextAlign align, int lineWidth, float lineHeight, float maxScale) {
            this.segments = segments;
            this.align = align;
            this.lineWidth = lineWidth;
            this.lineHeight = lineHeight;
            this.maxScale = maxScale;
        }
    }

    /**
     * All cached render data for the sign.
     */
    public static class CachedRenderData {
        public final List<CachedLine> lines;
        public final float totalHeight;

        public CachedRenderData(List<CachedLine> lines, float totalHeight) {
            this.lines = lines;
            this.totalHeight = totalHeight;
        }
    }

    public static class BBCSignBEState extends HolographicSignBlockEntityRenderer.HoloSignStateBase {
        public List<BBCodeHolographicSignBlockEntity.ParsedLine> parsedLines;
        public float defaultScale;
        public int defaultColor;
        public float defaultLineHeight;
        public float minWidth;
        public int globalBackgroundColor;
        public CachedRenderData cachedData;
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
        state.globalBackgroundColor = HolographicSignBlockEntityRenderer.getBackgroundColor(state);

        // Check if we need to build cached data
        if (be.clientCacheData instanceof CachedRenderData cached) {
            state.cachedData = cached;
        } else {
            state.cachedData = buildCachedData(be, this.font, state.globalBackgroundColor);
            be.clientCacheData = state.cachedData;
        }
    }

    /**
     * Build cached render data from parsed lines.
     */
    private CachedRenderData buildCachedData(BBCodeHolographicSignBlockEntity be, Font font, int globalBackgroundColor) {
        List<CachedLine> lines = new ArrayList<>();
        float totalHeight = 0;

        for (var line : be.parsedLines) {
            List<CachedSegment> segments = new ArrayList<>();
            int maxWidth = 0;
            float maxScale = be.defaultScale;
            float lineHeightMult = line.lineLineHeight != null ? line.lineLineHeight : be.defaultLineHeight;

            for (var segment : line.segments) {
                // Build component with TPAPI_PARSER + BBC styles
                Component styledComponent = buildSegmentComponent(segment, be, font);

                float segmentScale = segment.size() != null ? segment.size() : be.defaultScale;
                int segmentColor = segment.color() != null ? segment.color() : be.defaultColor;
                int segmentBgColor = segment.backgroundColor() != null ? segment.backgroundColor() : globalBackgroundColor;

                // Calculate width
                int segmentWidth = (int) (font.width(styledComponent.getString()) * segmentScale);

                segments.add(new CachedSegment(styledComponent, segmentScale, segmentColor, segmentBgColor, segmentWidth));

                if (segmentWidth > maxWidth) maxWidth = segmentWidth;
                if (segmentScale > maxScale) maxScale = segmentScale;
            }

            // Apply minimum width
            float minPixelWidth = be.minWidth * 40.0F;
            if (maxWidth < minPixelWidth) maxWidth = (int) minPixelWidth;

            float lineHeight = (font.lineHeight + 2) * lineHeightMult * maxScale;
            lines.add(new CachedLine(segments, line.lineAlign, maxWidth, lineHeight, maxScale));
            totalHeight += lineHeight;
        }

        return new CachedRenderData(lines, totalHeight);
    }

    /**
     * Build a styled component for a text segment.
     */
    private Component buildSegmentComponent(BBCodeHolographicSignBlockEntity.TextSegment segment, BBCodeHolographicSignBlockEntity be, Font font) {
        int segmentColor = segment.color() != null ? segment.color() : be.defaultColor;
        Boolean bold = segment.bold() != null ? segment.bold() : be.dropShadow;
        Boolean underline = segment.underline() != null ? segment.underline() : false;
        Boolean italic = segment.italic() != null ? segment.italic() : false;

        // Parse with TPAPI first
        Component parsed = MarkdownLiteParserV1.ALL
                .parseComponent(segment.text(), PlaceholderContext.of(Minecraft.getInstance().player).asParserContext());

        // Create BBC style from segment properties
        Style bbcStyle = Style.EMPTY
                .withColor(TextColor.fromRgb(segmentColor & 0xFFFFFF))
                .withBold(bold)
                .withUnderlined(underline)
                .withItalic(italic);

        // Merge: BBC as base, TPAPI parsed style on top (overrides)
        return parsed.copy().setStyle(parsed.getStyle().applyTo(bbcStyle));
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

        var cachedData = theSign.cachedData;
        if (cachedData == null || cachedData.lines.isEmpty()) {
            poseStack.popPose();
            return;
        }

        int bgColor = theSign.globalBackgroundColor;

        // Starting Y position (centered vertically)
        float currentY = -cachedData.totalHeight / 2.0F;

        for (var line : cachedData.lines) {
            var lineAlign = line.align;
            int lineWidth = line.lineWidth;
            float lineHeight = line.lineHeight;

            // Calculate starting X based on alignment
            float segmentStartX = switch (lineAlign) {
                case LEFT -> -lineWidth / 2.0F;
                case CENTER -> 0;
                case RIGHT -> lineWidth / 2.0F;
            };

            float currentX = segmentStartX;

            for (var segment : line.segments) {
                // Calculate X position for this segment based on line alignment
                float x = getX(lineAlign, currentX);

                // Render the segment with its specific scale and background color
                boolean isCenterAlign = lineAlign == BBCodeHolographicSignBlockEntity.TextAlign.CENTER;
                renderSegmentWithScale(poseStack, nodeCollector, segment.component, theSign.dropShadow,
                        segment.color, segment.backgroundColor, packedLight, x, currentY, segment.scale, segment.width, isCenterAlign);

                // Update currentX for next segment
                if (lineAlign == BBCodeHolographicSignBlockEntity.TextAlign.LEFT) {
                    currentX += segment.width;
                } else if (lineAlign == BBCodeHolographicSignBlockEntity.TextAlign.CENTER) {
                    currentX += segment.width;
                } else { // RIGHT
                    currentX -= segment.width;
                }
                segmentStartX = currentX;
            }

            // Move to next line
            currentY += lineHeight;
        }

        poseStack.popPose();
    }

    private static float getX(BBCodeHolographicSignBlockEntity.TextAlign lineAlign, float currentX) {
        return switch (lineAlign) {
            case LEFT -> currentX;
            case CENTER -> currentX;
            case RIGHT -> currentX;
        };
    }

    /**
     * Render a text segment with a specific scale.
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
            renderX = x - (float) width / 2.0F / scale;
        }

        // Render background if enabled
        if (backgroundColor != 0 && backgroundColor != org.teacon.powertool.utils.VanillaUtils.TRANSPARENT) {
            HolographicSignBlockEntityRenderer.renderBackground(poseStack, nodeCollector, backgroundColor, packedLight,
                    renderX - 1.0F / scale, y - 1.0F / scale, (int) ((float) width / scale));
        }

        // Render text with styling
        nodeCollector.submitText(poseStack, renderX, y, textComponent.getVisualOrderText(),
                dropShadow, Font.DisplayMode.POLYGON_OFFSET, packedLight, color, 0, 0);

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(BBCodeHolographicSignBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(8, 8, 8);
    }
}
