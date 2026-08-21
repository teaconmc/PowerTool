package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.xkball.xklibmc.api.client.b3d.SamplerCacheCache;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.JEIRecipeDisplayBlockEntity;
import org.teacon.powertool.client.b3d.DynamicRenderSetup;
import org.teacon.powertool.client.gui.JEIRecipeDisplayScreen;
import org.teacon.powertool.compat.jei.PowerToolJEIPlugin;
import org.teacon.powertool.utils.SizedCache;

import java.util.Map;
import java.util.Objects;

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
        if (PowerToolJEIPlugin.runtime != null && blockEntity.recipeType != null && blockEntity.recipeId != null) {
            var key = new RecipeKey(blockEntity.recipeType, blockEntity.recipeId, blockEntity.getBlockPos());
            state.cacheEntry = cache.getOrCreate(key, RecipeRenderCache::create);
            if (Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) {
                state.hit = blockHitResult.getBlockPos().equals(blockEntity.getBlockPos());
            } else state.hit = false;
            state.partialTicks = partialTicks;
            state.yRotation = blockEntity.yRotation;
        }
    }
    
    private void renderText(Component text, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        var w = Minecraft.getInstance().font.width(text);
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.6f, 0.5f);
        poseStack.scale(0.025f, -0.025f, 0.025f);
        poseStack.translate(-w / 2f, 0, 0);
        submitNodeCollector.submitText(poseStack, 0, 0, text.getVisualOrderText(), false, Font.DisplayMode.POLYGON_OFFSET, 15728880, -1, 0, 0);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.6f, 0.5f);
        poseStack.scale(0.025f, -0.025f, 0.025f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.translate(-w / 2f, 0, 0);
        submitNodeCollector.submitText(poseStack, 0, 0, text.getVisualOrderText(), false, Font.DisplayMode.POLYGON_OFFSET, 15728880, -1, 0, 0);
        poseStack.popPose();
    }
    
    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        var entry = state.cacheEntry;
        if (PowerToolJEIPlugin.runtime == null) {
            renderText(Component.translatable("powertool.jei_recipe_display.init_jei"), poseStack, submitNodeCollector);
            return;
        }
        if (entry == null || !entry.valid()) {
            renderText(Component.translatable("powertool.jei_recipe_display.unrecognized_recipe").withStyle(ChatFormatting.RED), poseStack, submitNodeCollector);
            return;
        }
        assert entry.renderType != null && entry.textureTarget != null;
        var cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity != null) {
            var eye = cameraEntity.getEyePosition(state.partialTicks);
            var dir = cameraEntity.getViewVector(state.partialTicks);
            var pair = entry.getWorldCorners(state.blockPos, state.yRotation).raycast(eye.toVector3f(), dir.toVector3f());
            var revDir = entry.renderState.revDir;
            poseStack.pushPose();
            poseStack.translate(0.5f, (1 - entry.getHeight() * 0.01f) / 2f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRotation));
            poseStack.scale(0.01f, 0.01f, 0.01f);
            submitNodeCollector.submitCustomGeometry(poseStack, entry.renderType, (pose, consumer) -> {
                var w = entry.getWidth();
                var h = entry.getHeight();
                float l, r, t, b, z;
                if (entry.extraSize()) {
                    l = -w / 2f - w / 4f - (revDir ? 256 : 0);
                    r = w / 2f + w / 4f + (revDir ? 0 : 256);
                    t = h + h / 4f;
                    b = 0 - h / 4f - 256;
                    z = revDir ? -0.01f : 0.01f;
                } else {
                    l = -w / 2f - w / 4f;
                    r = w / 2f + w / 4f;
                    t = h + h / 4f;
                    b = 0 - h / 4f;
                    z = 0;
                }
                var normalZ = revDir ? -1.0f : 1.0f;
                consumer.addVertex(pose, l, t, z).setUv(1, 1).setLight(15728880).setColor(-1).setNormal(0, 0, normalZ);
                consumer.addVertex(pose, r, t, z).setUv(0, 1).setLight(15728880).setColor(-1).setNormal(0, 0, normalZ);
                consumer.addVertex(pose, r, b, z).setUv(0, 0).setLight(15728880).setColor(-1).setNormal(0, 0, normalZ);
                consumer.addVertex(pose, l, b, z).setUv(1, 0).setLight(15728880).setColor(-1).setNormal(0, 0, normalZ);
                consumer.addVertex(pose, l, b, z).setUv(0, 0).setLight(15728880).setColor(-1).setNormal(0, 0, normalZ);
                consumer.addVertex(pose, r, b, z).setUv(1, 0).setLight(15728880).setColor(-1).setNormal(0, 0, normalZ);
                consumer.addVertex(pose, r, t, z).setUv(1, 1).setLight(15728880).setColor(-1).setNormal(0, 0, normalZ);
                consumer.addVertex(pose, l, t, z).setUv(0, 1).setLight(15728880).setColor(-1).setNormal(0, 0, normalZ);
            });
            poseStack.popPose();
            
            var mouse = pair == null ? null : pair.getFirst();
            var mouseXOld = entry.renderState.mouseX;
            var mouseYOld = entry.renderState.mouseY;
            var mouseX = mouse == null ? 0 : (int) (mouse.x * entry.getWidth() + entry.getWidth() / 4f);
            var mouseY = mouse == null ? 0 : (int) (mouse.y * entry.getHeight() + entry.getHeight() / 4f);
            entry.renderState.mouseX = mouseX;
            entry.renderState.mouseY = mouseY;
            entry.renderState.dirty = entry.renderState.dirty || mouseX != 0 || mouseY != 0 || mouseXOld != mouseX || mouseYOld != mouseY;
            entry.updateTextureSize(mouseX != 0 || mouseY != 0);
            entry.renderState.revDir = pair != null && pair.getSecond();
        } else {
            if (entry.renderState.mouseX != 0 || entry.renderState.mouseY != 0) {
                entry.renderState.mouseX = 0;
                entry.renderState.mouseY = 0;
                entry.renderState.dirty = true;
            }
        }
    }
    
    public static class RenderState extends BlockEntityRenderState {
        @Nullable RecipeRenderCache cacheEntry;
        boolean hit;
        float partialTicks;
        int yRotation;
    }
    
    public record RecipeKey(Identifier recipeType, Identifier recipeId, BlockPos pos) {
    
    }
    
    //todo 动态大小
    public static class RecipeRenderCacheState {
        public boolean dirty = false;
        public boolean revDir = false;
        public int mouseX = 0;
        public int mouseY = 0;
        public int sizeX = 0;
        public int sizeY = 0;
    }
    
    public record RecipeRenderCache(@Nullable IRecipeLayoutDrawable<?> layout, @Nullable TextureTarget textureTarget,
                                    @Nullable RenderType renderType,
                                    RecipeRenderCacheState renderState) implements AutoCloseable {
        
        public static RecipeRenderCache create(RecipeKey key) {
            var layout = JEIRecipeDisplayScreen.updateRecipeLayout(key.recipeType, key.recipeId);
            if (layout == null) {
                return new RecipeRenderCache(null, null, null, new RecipeRenderCacheState());
            }
            var rect = layout.getRect();
            var target = new TextureTarget(key.recipeId.getPath(), (int) (rect.getWidth() * 6f), (int) (rect.getHeight() * 6f), true);
            var renderSetupInner = RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK).useLightmap().createRenderSetup();
            var renderSetup = new DynamicRenderSetup(renderSetupInner,
                    () -> Map.of(
                    "Sampler0", new RenderSetup.TextureAndSampler(Objects.requireNonNull(target.getColorTextureView()), SamplerCacheCache.NEAREST_CLAMP),
                    "Sampler2", new RenderSetup.TextureAndSampler(Minecraft.getInstance().gameRenderer.lightmap(), SamplerCacheCache.LINEAR_CLAMP)));
            var renderType = RenderType.create("jei_recipe_display_ber", renderSetup);
            var renderState = new RecipeRenderCacheState();
            renderState.dirty = true;
            var result = new RecipeRenderCache(layout, target, renderType, renderState);
            result.updateTextureSize(false);
            return result;
        }
        
        private static Vector3f transform(Matrix4f mat, float x, float y, float z) {
            Vector4f v = new Vector4f(x, y, z, 1);
            v.mul(mat);
            return new Vector3f(v.x(), v.y(), v.z());
        }
        
        public boolean extraSize() {
            if (this.textureTarget == null) return false;
            return this.textureTarget.width != (int) (this.getWidth() * 6f) && this.textureTarget.height != (int) (this.getHeight() * 6f);
        }
        
        public void updateTextureSize(boolean extraSize) {
            if (this.layout == null) return;
            var rect = this.layout.getRect();
            if (extraSize) {
                this.renderState.sizeX = (int) (rect.getWidth() * 6f + 1024);
                this.renderState.sizeY = (int) (rect.getHeight() * 6f + 1024);
            } else {
                this.renderState.sizeX = (int) (rect.getWidth() * 6f);
                this.renderState.sizeY = (int) (rect.getHeight() * 6f);
            }
            
        }
        
        public boolean valid() {
            return layout != null;
        }
        
        public int getWidth() {
            return layout == null ? 0 : layout.getRect().getWidth();
        }
        
        public int getHeight() {
            return layout == null ? 0 : layout.getRect().getHeight();
        }
        
        public WorldQuad getWorldCorners(BlockPos pos, float yRotationDegrees) {
            if (this.textureTarget == null) {
                return new WorldQuad();
            }
            Matrix4f mat = new Matrix4f()
                    .translation(pos.getX() + 0.5f, pos.getY() + (1 - this.getHeight() * 0.01f) / 2f, pos.getZ() + 0.5f)
                    .rotateY((float) Math.toRadians(yRotationDegrees))
                    .scale(0.01f);
            
            float w = this.getWidth();
            float h = this.getHeight();
            
            return new WorldQuad(
                    transform(mat, -w / 2f, h, 0),
                    transform(mat, w / 2f, h, 0),
                    transform(mat, w / 2f, 0, 0),
                    transform(mat, -w / 2f, 0, 0)
            );
        }
        
        @Override
        public void close() {
            if (this.textureTarget != null) this.textureTarget.destroyBuffers();
        }
    }
    
    public record WorldQuad(
            Vector3f topLeft,
            Vector3f topRight,
            Vector3f bottomRight,
            Vector3f bottomLeft
    ) {
        public WorldQuad() {
            this(new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f());
        }
        
        public AABB getAABB() {
            float minX = Math.min(Math.min(topLeft.x, topRight.x), Math.min(bottomLeft.x, bottomRight.x));
            float minY = Math.min(Math.min(topLeft.y, topRight.y), Math.min(bottomLeft.y, bottomRight.y));
            float minZ = Math.min(Math.min(topLeft.z, topRight.z), Math.min(bottomLeft.z, bottomRight.z));
            float maxX = Math.max(Math.max(topLeft.x, topRight.x), Math.max(bottomLeft.x, bottomRight.x));
            float maxY = Math.max(Math.max(topLeft.y, topRight.y), Math.max(bottomLeft.y, bottomRight.y));
            float maxZ = Math.max(Math.max(topLeft.z, topRight.z), Math.max(bottomLeft.z, bottomRight.z));
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
        
        public @Nullable Pair<Vector2f, Boolean> raycast(Vector3f rayOrigin, Vector3f rayDir) {
            Vector3f p0 = topLeft;
            Vector3f uAxis = new Vector3f(topRight).sub(p0);
            Vector3f vAxis = new Vector3f(bottomLeft).sub(p0);
            Vector3f originLocal = new Vector3f(rayOrigin).sub(p0);
            float closestU = Math.clamp(originLocal.dot(uAxis) / uAxis.lengthSquared(), 0, 1);
            float closestV = Math.clamp(originLocal.dot(vAxis) / vAxis.lengthSquared(), 0, 1);
            Vector3f closest = new Vector3f(p0).add(new Vector3f(uAxis).mul(closestU)).add(new Vector3f(vAxis).mul(closestV));
            if (closest.distanceSquared(rayOrigin) > 64) return null;
            Vector3f normal = new Vector3f(uAxis).cross(vAxis);
            float denom = normal.dot(rayDir);
            // 平行
            if (Math.abs(denom) < 1e-6f) return null;
            // 射线与平面求交
            float t = new Vector3f(p0).sub(rayOrigin).dot(normal) / denom;
            // 交点在射线反方向
            if (t < 0) return null;
            // 交点
            Vector3f hit = new Vector3f(rayDir).mul(t).add(rayOrigin);
            Vector3f local = new Vector3f(hit).sub(p0);
            float u = local.dot(uAxis) / uAxis.lengthSquared();
            float v = local.dot(vAxis) / vAxis.lengthSquared();
            if (u < 0 || u > 1 || v < 0 || v > 1) {
                return null;
            }
            float texU = denom < 0 ? 1 - u : u;
            return Pair.of(new Vector2f(texU, v), denom < 0);
        }
    }
    
}
