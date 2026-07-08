package org.teacon.powertool.client.gui.exhibition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.teacon.powertool.client.gui.widget.ConfigurationHierarchy;
import org.teacon.powertool.client.gui.widget.Inspector;
import org.teacon.powertool.entity.exhibit.ExhibitionEntity;
import org.teacon.powertool.exhibition.node.ExhibitionNode;
import org.teacon.powertool.inspection.Inspectable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class ExhibitionEntityEditorScreen extends Screen {

    private final ConfigurationHierarchy    hierarchy;
    private final Inspector                 inspector;
    private final ExhibitionEntity          entity;

    private ExhibitionEntityEditorScreen(final ExhibitionEntity entity) {
        super(Component.literal("ExhibitionEntityEditor"));

        final var minecraft = Minecraft.getInstance();
        final var window    = minecraft.getWindow();

        this.hierarchy      = new ConfigurationHierarchy(150, window.getGuiScaledHeight());
        this.inspector      = new Inspector(150, window.getGuiScaledHeight());
        this.inspector      .setX(window.getGuiScaledWidth() - this.inspector.getWidth());

        this.addRenderableWidget(this.hierarchy);
        this.addRenderableWidget(this.inspector);

        this.entity         = entity;
        this.hierarchy.setRoot(entity.getExhibitionNode());
        this.hierarchy.setOnSelect(exhibitionNode -> {
            if (exhibitionNode instanceof Inspectable inspectable) {
                ExhibitionEntityEditorScreen.this.inspector.setInspectObject(inspectable);
            }
        });
    }

    public static Screen of(final ExhibitionEntity entity) {
        return new ExhibitionEntityEditorScreen(entity);
    }

    @Override
    public void extractRenderState(
            final GuiGraphicsExtractor  graphics,
            final int                   mouseX,
            final int                   mouseY,
            final float                 partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        /*EntityRenderState state = extractRenderState(this.entity);
        Quaternionf rotation = (new Quaternionf()).rotateZ((float)Math.PI);
        Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F, 0.0F);

        graphics.entity(
                state,
                30.0f,
                translation,
                rotation,
                new Quaternionf(),
                mouseX - 50, mouseY - 50,
                mouseX + 50, mouseY + 50
        );*/

    }

    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);

        this.inspector.setHeight(height);
        this.inspector.setX(width - this.inspector.getWidth());
    }

    private static EntityRenderState extractRenderState(
            final LivingEntity entity
    ) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        return renderState;
    }
}
