package org.teacon.powertool.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.teacon.powertool.entity.FenceKnotEntity;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FenceKnotRenderer extends EntityRenderer<FenceKnotEntity> {
    private static final ResourceLocation KNOT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/lead_knot.png");
    private final LeashKnotModel<FenceKnotEntity> model;

    public FenceKnotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LeashKnotModel<>(context.bakeLayer(ModelLayers.LEASH_KNOT));
    }

    @Override
    public ResourceLocation getTextureLocation(FenceKnotEntity entity) {
        return KNOT_LOCATION;
    }

    @Override
    public void render(FenceKnotEntity e, float yRot, float partialTick, PoseStack transform, MultiBufferSource buffers, int packedLight) {
        super.render(e, yRot, partialTick, transform, buffers, packedLight);
        transform.pushPose();
        transform.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(e, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = buffers.getBuffer(this.model.renderType(KNOT_LOCATION));
        this.model.renderToBuffer(transform, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, VanillaUtils.getColor(255,255,255,255));
        transform.popPose();
        for (var fromPos : e.getConnectTo()) {
            this.renderLeash(e, partialTick, transform, buffers, fromPos);
        }
        var mc = Minecraft.getInstance();
        var hitResult = mc.hitResult;
        if (mc.player != null && mc.player.getAbilities().instabuild && hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == e) {
            transform.pushPose();
            transform.mulPose(this.entityRenderDispatcher.cameraOrientation());
            transform.scale(-0.025F, -0.025F, 0.025F);
            Component tip = Component.translatable("entity.powertool.fence_knot");
            Component tipLine2 = Component.translatable("entity.powertool.fence_knot.tooltip");
            int transparency = ((int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255)) << 24;
            Font font = this.getFont();
            font.drawInBatch(tip, -font.width(tip) / 2.0F, -15, 0xFFFFFFFF, false, transform.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, transparency, packedLight);
            font.drawInBatch(tipLine2, -font.width(tipLine2) / 2.0F, -5, 0xFFFFFFFF, false, transform.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, transparency, packedLight);
            transform.popPose();
        }
    }
    private void renderLeash(FenceKnotEntity to, float partialTick, PoseStack transform, MultiBufferSource buffers, BlockPos fromPos) {
        // Copied from MobRenderer::renderLeash TODO: Explain what it does, and what we can simplify
        transform.pushPose();
        Vec3 vec3 = fromPos.getCenter();
        double d0 = /*(double)(Mth.lerp(partialTick, to.yBodyRotO, to.yBodyRot) * ((float)Math.PI / 180F)) +*/ (Math.PI / 2D);
        Vec3 vec31 = to.getLeashOffset(partialTick);
        double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
        double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
        double d3 = Mth.lerp(partialTick, to.xo, to.getX()) + d1;
        double d4 = Mth.lerp(partialTick, to.yo, to.getY()) + vec31.y;
        double d5 = Mth.lerp(partialTick, to.zo, to.getZ()) + d2;
        transform.translate(d1, vec31.y, d2);
        float f = (float)(vec3.x - d3);
        float f1 = (float)(vec3.y - d4);
        float f2 = (float)(vec3.z - d5);
        float f3 = 0.025F;
        VertexConsumer vertexconsumer = buffers.getBuffer(RenderType.leash());
        Matrix4f matrix4f = transform.last().pose();
        float f4 = Mth.invSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = BlockPos.containing(to.getEyePosition(partialTick));
        BlockPos blockpos1 = fromPos;
        int i = this.getBlockLightLevel(to, blockpos);
        int j = i;//this.entityRenderDispatcher.getRenderer(to).getBlockLightLevel(to, blockpos1);
        int k = to.level().getBrightness(LightLayer.SKY, blockpos);
        int l = to.level().getBrightness(LightLayer.SKY, blockpos1);

        for(int i1 = 0; i1 <= 24; ++i1) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, i1, false);
        }

        for(int j1 = 24; j1 >= 0; --j1) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, j1, true);
        }

        transform.popPose();
    }

    private static void addVertexPair(VertexConsumer vertexes, Matrix4f transform, float p_174310_, float p_174311_, float p_174312_, int p_174313_, int p_174314_, int p_174315_, int p_174316_, float p_174317_, float p_174318_, float p_174319_, float p_174320_, int p_174321_, boolean p_174322_) {
        // Copied from MobRenderer::addVertexPair, TODO: Explain what it does, and what we can simplify
        float f = (float)p_174321_ / 24.0F;
        int i = (int)Mth.lerp(f, (float)p_174313_, (float)p_174314_);
        int j = (int)Mth.lerp(f, (float)p_174315_, (float)p_174316_);
        int k = LightTexture.pack(i, j);
        float f1 = p_174321_ % 2 == (p_174322_ ? 1 : 0) ? 0.7F : 1.0F;
        float f2 = 0.5F * f1;
        float f3 = 0.4F * f1;
        float f4 = 0.3F * f1;
        float f5 = p_174310_ * f;
        float f6 = p_174311_ > 0.0F ? p_174311_ * f * f : p_174311_ - p_174311_ * (1.0F - f) * (1.0F - f);
        float f7 = p_174312_ * f;
        vertexes.addVertex(transform, f5 - p_174319_, f6 + p_174318_, f7 + p_174320_).setColor(f2, f3, f4, 1.0F).setLight(k);
        vertexes.addVertex(transform, f5 + p_174319_, f6 + p_174317_ - p_174318_, f7 - p_174320_).setColor(f2, f3, f4, 1.0F).setLight(k);
    }

}
