package org.teacon.powertool.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.CreativeBlockBreakUndo;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.utils.VanillaUtils;

@NonNullByDefault
public record UndoCreativeBlockBreakPacket() implements CustomPacketPayload {

    public static final UndoCreativeBlockBreakPacket INSTANCE = new UndoCreativeBlockBreakPacket();
    public static final Type<UndoCreativeBlockBreakPacket> TYPE = new Type<>(VanillaUtils.modRL("undo_creative_block_break"));
    public static final StreamCodec<ByteBuf, UndoCreativeBlockBreakPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.isCreative()) {
                CreativeBlockBreakUndo.undo(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
