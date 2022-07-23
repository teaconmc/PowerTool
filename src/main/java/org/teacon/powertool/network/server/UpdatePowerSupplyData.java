package org.teacon.powertool.network.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.teacon.powertool.menu.PowerSupplyMenu;

import java.util.function.Supplier;

public class UpdatePowerSupplyData {

    private int type, data;

    public UpdatePowerSupplyData(int type, int data) {
        this.type = type;
        this.data = data;
    }

    public UpdatePowerSupplyData(FriendlyByteBuf buf) {
        this.type = buf.readVarInt();
        this.data = buf.readVarInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.type).writeVarInt(this.data);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null) {
                if (player.containerMenu instanceof PowerSupplyMenu theMenu) {
                    if (this.type == 0) {
                        theMenu.dataHolder.status = this.data;
                        var callback = theMenu.dataHolder.markDirty;
                        if (callback != null) {
                            callback.run();
                        }
                    } else if (this.type == 1) {
                        theMenu.dataHolder.power = this.data;
                        var callback = theMenu.dataHolder.markDirty;
                        if (callback != null) {
                            callback.run();
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
