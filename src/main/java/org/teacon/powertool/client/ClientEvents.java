package org.teacon.powertool.client;

import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.network.capability.Permission;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void on(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof AbstractClientPlayer) {
            event.addCapability(Permission.KEY, new Permission.Provider());
        }
    }

    @SubscribeEvent
    public static void on(ScreenOpenEvent event) {
        if (event.getScreen() instanceof CommandBlockEditScreen screen
            && screen.autoCommandBlock instanceof PeriodicCommandBlockEntity blockEntity) {
            event.setScreen(new PeriodicCommandBlockEditScreen(blockEntity));
        }
    }
}
