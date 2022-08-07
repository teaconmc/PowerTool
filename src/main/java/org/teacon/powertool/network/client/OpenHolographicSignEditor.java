package org.teacon.powertool.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.teacon.powertool.block.entity.HolographicSignBlockEntity;
import org.teacon.powertool.client.HolographicSignEditingScreen;

import java.util.function.Supplier;

public class OpenHolographicSignEditor {

    private BlockPos location;

    public OpenHolographicSignEditor(BlockPos location) {
        this.location = location;
    }

    public OpenHolographicSignEditor(FriendlyByteBuf buf) {
        this.location = buf.readBlockPos();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.location);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        context.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            var level = mc.level;
            if (level != null && level.getBlockEntity(this.location) instanceof HolographicSignBlockEntity theSign) {
                mc.setScreen(new HolographicSignEditingScreen(theSign, mc.isTextFilteringEnabled()));
            }
        });
        context.setPacketHandled(true);
    }


}
