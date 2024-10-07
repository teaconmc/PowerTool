package org.teacon.powertool.command;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.teacon.powertool.PowerTool;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = PowerTool.MODID)
public class PowerToolCommands {

    @SubscribeEvent
    public static void regCommand(RegisterCommandsEvent event) {
        FlyCommand.reg(event.getDispatcher());
        BatHappyCommand.register(event.getDispatcher());
    }
}
