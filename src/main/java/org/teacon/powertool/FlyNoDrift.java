package org.teacon.powertool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.client.FlyNoDriftClient;
import org.teacon.powertool.network.client.UpdateFlyNoDriftPacket;

public final class FlyNoDrift {
    private  FlyNoDrift() {
    }

    public static boolean canNoDrift(Player player) {
        return player.isSpectator() || enabled(player) && player.getAbilities().flying;
    }

    public static boolean enabled(Player player) {
        if (player.level().isClientSide && FMLEnvironment.getDist() == Dist.CLIENT) {
            return FlyNoDriftClient.enabled(player);
        }
        return player.getData(PowerToolAttachments.FLY_NO_DRIFT);
    }

    public static void setEnabled(ServerPlayer player, boolean enabled) {
        player.setData(PowerToolAttachments.FLY_NO_DRIFT, enabled);
        sync(player);
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new UpdateFlyNoDriftPacket(enabled(player)));
    }
}
