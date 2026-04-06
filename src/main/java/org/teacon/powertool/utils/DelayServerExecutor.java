package org.teacon.powertool.utils;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.teacon.powertool.PowerTool;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

//真的优雅吗? --xkball
@EventBusSubscriber(modid = PowerTool.MODID)
public class DelayServerExecutor {
    
    private DelayServerExecutor() {
    }
    
    @Nonnull
    private static List<Task> tasks = new ArrayList<>();
    
    public static void addTask(int tickDelay, Consumer<MinecraftServer> task) {
        tasks.add(new Task(tickDelay, task));
    }
    
    @SubscribeEvent
    public static void afterServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var tasksNew = new ArrayList<Task>();
        for (var task : tasks) {
            if (task.tickDelay <= 0) task.task.accept(server);
            else tasksNew.add(new Task(task.tickDelay - 1, task.task));
        }
        tasks = tasksNew;
    }
    
    private record Task(int tickDelay, Consumer<MinecraftServer> task) {
    }
}
