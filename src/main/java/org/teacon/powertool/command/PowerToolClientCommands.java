package org.teacon.powertool.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.client.PanoramicScreenShotHelper;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = PowerTool.MODID)
public class PowerToolClientCommands {
    
    @SubscribeEvent
    public static void onRegClientCommand(RegisterClientCommandsEvent event){
        event.getDispatcher().register(
                Commands.literal("powertool")
                        .redirect(event.getDispatcher().register(Commands.literal("pt")
                                        .then(Commands.literal("client")
                                                .then(Commands.literal("panoramic_screenshot")
                                                        .then(Commands.argument("height", IntegerArgumentType.integer(1,16384))
                                                                .then(Commands.argument("fov", IntegerArgumentType.integer(1,180))
                                                                        .then(Commands.argument("pitch", IntegerArgumentType.integer(-90,90))
                                                                                .executes(PanoramicScreenShotHelper.INSTANCE::start))))
                                        )
                                )
                        
                        )));
    }
}
