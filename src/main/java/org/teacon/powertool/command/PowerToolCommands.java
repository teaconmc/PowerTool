package org.teacon.powertool.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.teacon.powertool.PowerTool;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = PowerTool.MODID)
public class PowerToolCommands {

    @SubscribeEvent
    public static void regCommand(RegisterCommandsEvent event) {
        FlyCommand.reg(event.getDispatcher());
    }
}
