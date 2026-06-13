package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.xkball.xklibmc.api.client.b3d.SamplerCacheCache;
import com.xkball.xklibmc.client.b3d.pipeline.ExtendedRenderPipeline;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.JEIRecipeDisplayBlockEntity;
import org.teacon.powertool.client.gui.JEIRecipeDisplayScreen;
import org.teacon.powertool.compat.jei.PowerToolJEIPlugin;
import org.teacon.powertool.utils.SizedCache;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@NonNullByDefault
public class JEIRecipeDisplayBlockEntityRenderer implements BlockEntityRenderer<JEIRecipeDisplayBlockEntity, JEIRecipeDisplayBlockEntityRenderer.RenderState> {
    
    @Nullable
    public static SizedCache<RecipeKey, RecipeRenderCache> recipeLayoutCache;

    private final SizedCache<RecipeKey, RecipeRenderCache> cache;
    

    public JEIRecipeDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.cache = new SizedCache<>();
        recipeLayoutCache = this.cache;
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(JEIRecipeDisplayBlockEntity blockEntity, RenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if(PowerToolJEIPlugin.runtime != null && blockEntity.recipeType != null && blockEntity.recipeId != null){
            var key = new RecipeKey(blockEntity.recipeType, blockEntity.recipeId);
            state.cacheEntry = cache.getOrCreate(key, RecipeRenderCache::create);
        }
    }
    
    private void renderText(Component text, PoseStack poseStack, SubmitNodeCollector submitNodeCollector){
        var w = Minecraft.getInstance().font.width(text);
        poseStack.pushPose();
        poseStack.translate(0.5f,0.6f,0);
        poseStack.scale(0.025f, -0.025f, 0.025f);
        poseStack.translate(-w/2f,0,0);
        submitNodeCollector.submitText(poseStack, 0, 0, text.getVisualOrderText(),false, Font.DisplayMode.POLYGON_OFFSET, 15728880, -1, 0, 0);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5f,0.6f,0);
        poseStack.scale(0.025f, -0.025f, 0.025f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.translate(-w/2f,0,0);
        submitNodeCollector.submitText(poseStack, 0, 0, text.getVisualOrderText(),false, Font.DisplayMode.POLYGON_OFFSET, 15728880, -1, 0, 0);
        poseStack.popPose();
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        var entry = state.cacheEntry;
        if (PowerToolJEIPlugin.runtime == null) {
            renderText(Component.translatable("powertool.jei_recipe_display.init_jei"), poseStack, submitNodeCollector);
            return;
        }
        if(entry == null || entry.layout == null){
            renderText(Component.translatable("powertool.jei_recipe_display.unrecognized_recipe").withStyle(ChatFormatting.RED), poseStack, submitNodeCollector);
            return;
        }
        assert entry.renderType != null && entry.textureTarget != null;
        poseStack.pushPose();
        poseStack.translate(0.5f,(1-entry.textureTarget.height * 0.01f)/2f,0.5f);
        poseStack.scale(0.01f, 0.01f, 0.01f);
        submitNodeCollector.submitCustomGeometry(poseStack, entry.renderType, (pose, consumer) -> {
            var w = entry.textureTarget.width;
            var h = entry.textureTarget.height;
            consumer.addVertex(pose,-w / 2f, h, 0).setUv(1, 1).setColor(-1);
            consumer.addVertex(pose,w / 2f, h, 0).setUv(0, 1).setColor(-1);
            consumer.addVertex(pose,w / 2f, 0, 0).setUv(0, 0).setColor(-1);
            consumer.addVertex(pose,-w / 2f, 0, 0).setUv(1, 0).setColor(-1);
            consumer.addVertex(pose, -w / 2f, 0, 0).setUv(0, 0).setColor(-1);
            consumer.addVertex(pose,  w / 2f, 0, 0).setUv(1, 0).setColor(-1);
            consumer.addVertex(pose,  w / 2f, h, 0).setUv(1, 1).setColor(-1);
            consumer.addVertex(pose, -w / 2f, h, 0).setUv(0, 1).setColor(-1);
        });
        poseStack.popPose();
    }

    public static class RenderState extends BlockEntityRenderState {
        @Nullable RecipeRenderCache cacheEntry;
    }
    
    public record RecipeKey(Identifier recipeType, Identifier recipeId){
    
    }
    
    public record RecipeRenderCache(@Nullable IRecipeLayoutDrawable<?> layout, @Nullable TextureTarget textureTarget, @Nullable RenderType renderType, AtomicBoolean dirty){
    
        public static RecipeRenderCache create(RecipeKey key){
            var layout = JEIRecipeDisplayScreen.updateRecipeLayout(key.recipeType, key.recipeId);
            if(layout == null){
                return new RecipeRenderCache(null, null, null, new AtomicBoolean(false));
            }
            var rect = layout.getRect();
            var target = new TextureTarget(key.recipeId.getPath(), rect.getWidth(), rect.getHeight(), true);
            var pipeline = ExtendedRenderPipeline.extendedbuilder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                    .withLocation(VanillaUtils.modRL(key.recipeId.getPath()))
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .bindSampler("Sampler0", () -> Pair.of(Objects.requireNonNull(target.getColorTextureView()), SamplerCacheCache.NEAREST_CLAMP))
                    .buildExtended();
            var renderType = RenderType.create("jei_recipe_display_ber", RenderSetup.builder(pipeline).createRenderSetup());
            return new RecipeRenderCache(layout, target, renderType, new AtomicBoolean(true));
        }
    }
}
