package org.teacon.powertool.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record SetCommandBlockPacket(BlockPos pos, int period) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<SetCommandBlockPacket> TYPE = new Type<>(VanillaUtils.modResourceLocation("set_command_block_packet"));
    
    public static final StreamCodec<ByteBuf,SetCommandBlockPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,SetCommandBlockPacket::pos,
            ByteBufCodecs.INT,SetCommandBlockPacket::period,
            SetCommandBlockPacket::new
    );
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof PeriodicCommandBlockEntity blockEntity) {
                blockEntity.setPeriod(period);
            }
        });
        
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
