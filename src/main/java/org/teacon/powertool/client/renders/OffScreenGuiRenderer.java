package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.GuiBannerResultRenderer;
import net.minecraft.client.gui.render.pip.GuiBookModelRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.GuiProfilerChartRenderer;
import net.minecraft.client.gui.render.pip.GuiSignRenderer;
import net.minecraft.client.gui.render.pip.GuiSkinRenderer;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.profiling.Profiler;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class OffScreenGuiRenderer {

    private static OffScreenGuiRenderer INSTANCE;
    
    private final Minecraft minecraft;
    private final FogRenderer fogRenderer;
    private final GuiRenderState guiRenderState;
    private final GuiRenderer guiRenderer;
    
    public OffScreenGuiRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        var bufferSource = minecraft.renderBuffers().bufferSource();
        this.fogRenderer = new FogRenderer();
        this.guiRenderState = new GuiRenderState();
        var gameRenderer = minecraft.gameRenderer;
        var levelRenderer = minecraft.levelRenderer;
        var atlasManager = minecraft.getAtlasManager();
        this.guiRenderer = new GuiRenderer(
                guiRenderState, bufferSource, levelRenderer.submitNodeStorage,
                gameRenderer.featureRenderDispatcher,
                net.neoforged.neoforge.client.ClientHooks.gatherPictureInPictureRenderers(List.of(
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState.class, buffers -> new GuiEntityRenderer(buffers, minecraft.getEntityRenderDispatcher())),
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiSkinRenderState.class, GuiSkinRenderer::new),
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiBookModelRenderState.class, GuiBookModelRenderer::new),
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiBannerResultRenderState.class, buffers -> new GuiBannerResultRenderer(buffers, atlasManager)),
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiSignRenderState.class, buffers -> new GuiSignRenderer(buffers, atlasManager)),
                        new net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration<>(net.minecraft.client.renderer.state.gui.pip.GuiProfilerChartRenderState.class, GuiProfilerChartRenderer::new)
                ))
        );
        
    }
    
    public static OffScreenGuiRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OffScreenGuiRenderer(Minecraft.getInstance());
        }
        return INSTANCE;
    }
    
    public void render(TextureTarget target, Consumer<GuiGraphicsExtractor> drawFunc){
        var graphics = new GuiGraphicsExtractor(this.minecraft, this.guiRenderState, 0, 0);
        var cmdEncoder = RenderSystem.getDevice().createCommandEncoder();
        cmdEncoder.clearColorAndDepthTextures(Objects.requireNonNull(target.getColorTexture()), 0, Objects.requireNonNull(target.getDepthTexture()), 1.0);
        graphics.nextStratum();
        drawFunc.accept(graphics);
        this.guiRenderer.prepare();
        this.drawToTarget(this.guiRenderer, target, this.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        for (var mesh : this.guiRenderer.meshesToDraw) {
            mesh.close();
        }
        for (MappableRingBuffer buffer : this.guiRenderer.vertexBuffers.values()) {
            buffer.rotate();
        }
        this.guiRenderer.draws.clear();
        this.guiRenderer.meshesToDraw.clear();
        this.guiRenderer.renderState.reset();
        this.guiRenderer.firstDrawIndexAfterBlur = Integer.MAX_VALUE;
        this.guiRenderer.clearUnusedOversizedItemRenderers();
        //noinspection UnstableApiUsage
        this.guiRenderer.pictureInPictureRendererPools.values().forEach(PictureInPictureRendererPool::clearUnusedRenderers);
        this.guiRenderer.endFrame();
    }

    private void drawToTarget(GuiRenderer guiRenderer, RenderTarget target, GpuBufferSlice fogBuffer) {
        if (guiRenderer.draws.isEmpty()) {
            return;
        }
        guiRenderer.guiProjection.setupOrtho(1000.0F, 11000.0F, target.width, target.height, true);
        RenderSystem.setProjectionMatrix(guiRenderer.guiProjectionMatrixBuffer.getBuffer(guiRenderer.guiProjection), ProjectionType.ORTHOGRAPHIC);

        int maxIndexCount = 0;
        for (var draw : guiRenderer.draws) {
            if (draw.indexCount() > maxIndexCount) {
                maxIndexCount = draw.indexCount();
            }
        }
        var autoIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = autoIndices.getBuffer(maxIndexCount);
        VertexFormat.IndexType indexType = autoIndices.type();
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(new Matrix4f().setTranslation(0.0F, 0.0F, -11000.0F), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());

        guiRenderer.executeDrawRange(() -> "offscreen_gui", target, fogBuffer, dynamicTransforms, indexBuffer, indexType, 0, guiRenderer.draws.size());
    }
}
