package org.teacon.powertool.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.teacon.powertool.PowerTool;

@EventBusSubscriber(modid = PowerTool.MODID)
public class PowerToolCommands {
    
    @SubscribeEvent
    public static void regCommand(RegisterCommandsEvent event) {
        FlyCommand.reg(event.getDispatcher());
        AccelerateCommand.register(event.getDispatcher());
        CreativeNoClipCommand.register(event.getDispatcher());
        FlyNoDriftCommand.register(event.getDispatcher());
        
        var setGameTimeCommand =
                Commands.literal("setGameTime")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes((c) -> {
                                    var v = IntegerArgumentType.getInteger(c,"value");
                                    var worldData = c.getSource().getLevel().getServer().getWorldData();
                                    if(worldData instanceof PrimaryLevelData data){
                                        data.setGameTime(v);
                                    }
                                    return 1;
                                }));
        event.getDispatcher().register(Commands.literal("powertool").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).then(setGameTimeCommand));
        event.getDispatcher().register(Commands.literal("pt").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).then(setGameTimeCommand));
    }
    
}
