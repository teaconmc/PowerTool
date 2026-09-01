package org.teacon.powertool.command;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.teacon.powertool.PowerTool;

@EventBusSubscriber(modid = PowerTool.MODID)
public class PowerToolClientCommands {
    
    @SubscribeEvent
    public static void onRegClientCommand(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("powertool")
                        .redirect(event.getDispatcher().register(Commands.literal("pt")
                                )
                        ));
    }
}
