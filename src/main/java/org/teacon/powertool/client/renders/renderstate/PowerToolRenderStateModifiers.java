package org.teacon.powertool.client.renders.renderstate;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.client.renders.entity.ExhibitionHumanoidRenderer;

public final class PowerToolRenderStateModifiers {



    @EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
    private static final class Handler {
        @SubscribeEvent
        public static void onRegister(final RegisterRenderStateModifiersEvent event) {

            event.registerEntityModifier(
                    ExhibitionHumanoidRenderer.class,
                    ExhibitionHumanoidModifier.INSTANCE
            );

        }
    }

}
