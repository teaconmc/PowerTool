package org.teacon.powertool.network.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;

import java.util.function.Supplier;

public record SetCommandBlockPacket(BlockPos pos, int period) {

    public SetCommandBlockPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readVarInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(period);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        var context = ctx.get();
        context.enqueueWork(() -> {
            var level = context.getSender().level();
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof PeriodicCommandBlockEntity blockEntity) {
                blockEntity.setPeriod(period);
            }
        });
        context.setPacketHandled(true);
    }
}
