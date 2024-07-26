package org.teacon.powertool.motd;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.teacon.powertool.PowerTool;

@EventBusSubscriber(modid = PowerTool.MODID)
public class MotDHandler {

    public static Component motd = null;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (motd != null) {
            event.getEntity().displayClientMessage(motd, false);
        }
    }
}
