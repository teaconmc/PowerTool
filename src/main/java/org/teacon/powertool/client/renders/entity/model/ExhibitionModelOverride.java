package org.teacon.powertool.client.renders.entity.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
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

            final var position  = new Vector3f(override.getPosition()).mul(0.0625f);
            final var rotation  = new Vector3f(override.getRotation()).mul(-Mth.DEG_TO_RAD);
            final var scale     = override.getScale();

            part.offsetPos(position);
            part.offsetRotation(rotation);

            part.xScale = scale.x();
            part.yScale = scale.y();
            part.zScale = scale.z();

            part.visible = override.isVisible();

        }
    }

}
