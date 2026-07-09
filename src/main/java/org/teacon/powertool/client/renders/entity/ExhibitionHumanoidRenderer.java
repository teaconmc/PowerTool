package org.teacon.powertool.client.renders.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.client.entity.ClientExhibitionHumanoid;
import org.teacon.powertool.entity.exhibit.ExhibitionHumanoid;
import org.teacon.powertool.exhibition.ExhibitionNodeManager;
import org.teacon.powertool.exhibition.node.ExhibitionNode;
import org.teacon.powertool.exhibition.node.SkinNode;

public class ExhibitionHumanoidRenderer extends HumanoidMobRenderer<ExhibitionHumanoid, AvatarRenderState, PlayerModel> {

    public static @NonNull ExhibitionHumanoidRenderer regular(
            final EntityRendererProvider.Context    context
    ) {
        return new ExhibitionHumanoidRenderer(
                context,
                false
        );
    }

    public static @NonNull ExhibitionHumanoidRenderer slim(
            final EntityRendererProvider.Context    context
    ) {
        return new ExhibitionHumanoidRenderer(
                context,
                true
        );
    }

    public ExhibitionHumanoidRenderer(
            final EntityRendererProvider.Context    context,
            final boolean                           slimSteve
    ) {
        super(
                context,
                new PlayerModel(context.bakeLayer(slimSteve ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slimSteve),
                0.5f
        );

        this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(slimSteve ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR, context.getModelSet(), (part) -> new PlayerModel(part, slimSteve)), context.getEquipmentRenderer()));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new ArrowLayer<>(this, context));
        this.addLayer(new CapeLayer(this, context.getModelSet(), context.getEquipmentAssets()));
        this.addLayer(new WingsLayer<>(this, context.getModelSet(), context.getEquipmentRenderer()));
    }

    @Override
    public void extractRenderState(
            final ExhibitionHumanoid    entity,
            final AvatarRenderState     state,
            final float                 partialTicks
    ) {
        super.extractRenderState(entity, state, partialTicks);
        state.skin = ((ClientExhibitionHumanoid) entity).getSkin();

    }

    @Override
    public @NonNull Identifier getTextureLocation(
            final AvatarRenderState state
    ) {
        return state.skin.body().texturePath();
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }
}
