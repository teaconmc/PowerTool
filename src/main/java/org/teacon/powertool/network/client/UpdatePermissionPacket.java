package org.teacon.powertool.network.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.teacon.powertool.network.capability.Permission;

import java.util.function.Supplier;

public record UpdatePermissionPacket(boolean canUseGameMasterBlock, boolean canSwitchGameMode, boolean canUseSelector) {

    public UpdatePermissionPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(canUseGameMasterBlock);
        buf.writeBoolean(canSwitchGameMode);
        buf.writeBoolean(canUseSelector);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        var context = ctx.get();
        context.enqueueWork(() -> {
            var minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.getCapability(Permission.CAPABILITY).ifPresent(it -> {
                    it.setCanSwitchGameMode(canSwitchGameMode);
                    it.setCanUseGameMasterBlock(canUseGameMasterBlock);
                    it.setCanUseSelector(canUseSelector);
                });
            }
        });
        context.setPacketHandled(true);
    }
}
