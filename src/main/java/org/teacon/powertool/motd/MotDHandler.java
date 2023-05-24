package org.teacon.powertool.motd;

import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.powertool.PowerTool;

@Mod.EventBusSubscriber(modid = PowerTool.MODID)
public class MotDHandler {

    public static Component motd = null;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (motd != null) {
            event.getEntity().displayClientMessage(motd, false);
        }
    }
}
