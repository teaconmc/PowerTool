package org.teacon.powertool.client.renders.entity.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.teacon.powertool.exhibition.node.ModelPartNode;
import org.teacon.powertool.exhibition.node.PoseNode;

import java.util.List;

public final class ExhibitionModelOverride {

    private final List<ModelPart> parts;

    public ExhibitionModelOverride(final List<ModelPart> parts) {
        this.parts = parts;
    }

    public void apply(final PoseNode node) {

        final var overrides = node.getParts();
        final var size      = parts.size();

        if (overrides.size() != size) {
            return;
        }

        // assume they are in the same structure

        for (int i = 0; i < size; i++) {

            final var override  = overrides.get(i);
            final var part      = this.parts.get(i);

            final var position  = override.getPosition();
            final var rotation  = override.getRotation();
            final var scale     = override.getScale();

            part.setPos(
                    position.x() * 0.0625f,
                    position.y() * 0.0625f,
                    position.z() * 0.0625f
            );

            part.setRotation(
                    rotation.x() * Mth.DEG_TO_RAD,
                    rotation.y() * Mth.DEG_TO_RAD,
                    rotation.z() * Mth.DEG_TO_RAD
            );

            part.xScale = scale.x() * 0.0625f;
            part.yScale = scale.y() * 0.0625f;
            part.zScale = scale.z() * 0.0625f;

        }
    }

}
