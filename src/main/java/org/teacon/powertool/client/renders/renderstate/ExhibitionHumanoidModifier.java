package org.teacon.powertool.client.renders.renderstate;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.teacon.powertool.entity.exhibit.ExhibitionHumanoid;

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

    }

}
