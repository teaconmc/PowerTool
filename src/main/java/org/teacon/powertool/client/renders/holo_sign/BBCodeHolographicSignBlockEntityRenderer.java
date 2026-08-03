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

    public BBCodeHolographicSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    /**
     * Cached render data for a single text segment.
     */
    public record CachedSegment(Component component, float scale, int color, int backgroundColor, float width) {
    }

    /**
     * Cached render data for a single line.
     */
    public record CachedLine(List<CachedSegment> segments, BBCodeHolographicSignBlockEntity.TextAlign align,
                             float lineWidth, float lineHeight, float maxScale) {
    }

    /**
     * All cached render data for the sign.
     */
    public record CachedRenderData(List<CachedLine> lines, float totalMaxWidth, float totalHeight) {
    }

    public static class BBCSignBEState extends HolographicSignBlockEntityRenderer.HoloSignStateBase {
        public @Nullable List<BBCodeHolographicSignBlockEntity.ParsedLine> parsedLines;
        public float defaultScale;
        public int defaultColor;
        public float defaultLineHeight;
        public float minWidth;
        public int globalBackgroundColor;
        public @Nullable CachedRenderData cachedData;
    }

    @Override
    public BBCSignBEState createRenderState() {
        return new BBCSignBEState();
    }

    @Override
    public void extractRenderState(BBCodeHolographicSignBlockEntity be, BBCSignBEState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        Font font = Minecraft.getInstance().font;
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
            state.cachedData = buildCachedData(be, font, state.globalBackgroundColor);
            be.clientCacheData = state.cachedData;
        }
    }

    /**
     * Build cached render data from parsed lines.
     */
    private CachedRenderData buildCachedData(BBCodeHolographicSignBlockEntity be, Font font, int globalBackgroundColor) {
        List<CachedLine> lines = new ArrayList<>();
        float totalHeight = 0;
        float totalMaxWidth = be.minWidth * 40.0F;

        for (var line : be.parsedLines) {
            List<CachedSegment> segments = new ArrayList<>();
            float lineWidth = 0.0f;
            float maxScale = be.defaultScale;
            float lineHeightMult = line.lineLineHeight != null ? line.lineLineHeight : be.defaultLineHeight;

            for (var segment : line.segments) {
                // Build component with TPAPI_PARSER + BBC styles
                Component styledComponent = buildSegmentComponent(segment, be);

                float segmentScale = segment.size() != null ? segment.size() : be.defaultScale;
                int segmentColor = segment.color() != null ? segment.color() : be.defaultColor;
                int segmentBgColor = segment.backgroundColor() != null ? segment.backgroundColor() : globalBackgroundColor;

                // Calculate width
                float segmentWidth = (font.width(styledComponent.getString()) * segmentScale);

                segments.add(new CachedSegment(styledComponent, segmentScale, segmentColor, segmentBgColor, segmentWidth));

                lineWidth += segmentWidth;
                if (segmentScale > maxScale) maxScale = segmentScale;
            }


            float lineHeight = (font.lineHeight + 2) * lineHeightMult * maxScale;
            lines.add(new CachedLine(segments, line.lineAlign, lineWidth, lineHeight, maxScale));
            totalHeight += lineHeight;
            totalMaxWidth = Math.max(lineWidth, totalMaxWidth);
        }

        return new CachedRenderData(lines, totalMaxWidth, totalHeight);
    }

    /**
     * Build a styled component for a text segment.
     */
    private Component buildSegmentComponent(BBCodeHolographicSignBlockEntity.TextSegment segment, BBCodeHolographicSignBlockEntity be) {
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

        // Starting Y position (centered vertically)
        float currentY = -cachedData.totalHeight / 2.0F;

        for (var line : cachedData.lines) {
            var lineAlign = line.align;
            float lineWidth = line.lineWidth;
            float lineHeight = line.lineHeight;

            // Calculate starting X based on alignment

            float currentX = switch (lineAlign) {
                case LEFT -> -cachedData.totalMaxWidth / 2.0F;
                case CENTER -> -lineWidth / 2.0F;
                case RIGHT -> cachedData.totalMaxWidth / 2.0F - lineWidth;
            };

            for (var segment : line.segments) {
                float x = currentX;

                renderSegmentWithScale(poseStack, nodeCollector, segment.component, theSign.dropShadow,
                        segment.color, segment.backgroundColor, packedLight, x, currentY, segment.scale, segment.width);

                // Update currentX for next segment
                currentX += segment.width;
            }

            // Move to next line
            currentY += lineHeight;
        }

        poseStack.popPose();
    }

    /**
     * Render a text segment with a specific scale.
     */
    private void renderSegmentWithScale(PoseStack poseStack, SubmitNodeCollector nodeCollector,
                                        Component textComponent, boolean dropShadow, int color, int backgroundColor, int packedLight,
                                        float x, float y, float scale, float width) {

        poseStack.pushPose();

        // Apply scale for this segment
        poseStack.translate(x, y, 0);
        poseStack.scale(scale, scale, 1.0F);
        poseStack.translate(-x, -y, 0);

        // Calculate render position based on alignment

        // Render background if enabled
        if (backgroundColor != 0 && backgroundColor != org.teacon.powertool.utils.VanillaUtils.TRANSPARENT) {
            HolographicSignBlockEntityRenderer.renderBackground(poseStack, nodeCollector, backgroundColor, packedLight,
                    x - 1.0F / scale, y - 1.0F / scale, (int) (width / scale));
        }

        // Render text with styling
        nodeCollector.submitText(poseStack, x, y, textComponent.getVisualOrderText(),
                dropShadow, Font.DisplayMode.POLYGON_OFFSET, packedLight, color, 0, 0);

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(BBCodeHolographicSignBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(8, 8, 8);
    }
}
