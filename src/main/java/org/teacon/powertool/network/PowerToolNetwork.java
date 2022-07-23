package org.teacon.powertool.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.network.client.UpdatePermissionPacket;
import org.teacon.powertool.network.server.SetCommandBlockPacket;
import org.teacon.powertool.network.server.UpdatePowerSupplyData;

import java.util.Optional;

public class PowerToolNetwork {

    private static SimpleChannel channel;

    public static void register() {
        channel = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PowerTool.MODID, "ch"),
            () -> "1",
            NetworkRegistry.acceptMissingOr("1"::equals),
            "1"::equals
        );
        channel.registerMessage(0, SetCommandBlockPacket.class, SetCommandBlockPacket::write,
            SetCommandBlockPacket::new, SetCommandBlockPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(1, UpdatePermissionPacket.class, UpdatePermissionPacket::write,
            UpdatePermissionPacket::new, UpdatePermissionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(2, UpdatePowerSupplyData.class, UpdatePowerSupplyData::write,
            UpdatePowerSupplyData::new, UpdatePowerSupplyData::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static SimpleChannel channel() {
        return channel;
    }
}
