package org.teacon.powertool.client.renders.renderstate;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.teacon.powertool.entity.exhibit.ExhibitionHumanoid;
import org.teacon.powertool.exhibition.node.PoseNode;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
public class ExhibitionHumanoidModifier implements BiConsumer<ExhibitionHumanoid, AvatarRenderState> {

    public static final ExhibitionHumanoidModifier INSTANCE = new ExhibitionHumanoidModifier();

    private ExhibitionHumanoidModifier() { }

    @Override
    public void accept(
            final ExhibitionHumanoid    humanoid,
            final AvatarRenderState     state
    ) {
        final var node = humanoid.getExhibitionNode().getUnique(PoseNode.UNIQUE_KEY);
        state.setRenderData(PoseNode.UNIQUE_KEY, node);
    }

}
