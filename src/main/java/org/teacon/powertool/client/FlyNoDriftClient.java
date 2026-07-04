package org.teacon.powertool.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class FlyNoDriftClient {
    private static boolean enabled;

    private FlyNoDriftClient() {

    }

    public static boolean enabled() {
        return enabled;
    }

    public static boolean enabled(Player player) {
        return enabled && player == Minecraft.getInstance().player;
    }

    public static void setEnabled(boolean enabled) {
        FlyNoDriftClient.enabled = enabled;
    }
}
