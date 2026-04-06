package org.teacon.powertool.network.client;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.client.AccessControlClient;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record UpdateStaticModeChunkDataPacket(int chunkX, int chunkZ,
                                              List<BlockPos> blockPosList) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<UpdateStaticModeChunkDataPacket> TYPE = new CustomPacketPayload.Type<>(VanillaUtils.modRL("static_mode_chunk_data"));
    
    public static final StreamCodec<ByteBuf, UpdateStaticModeChunkDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateStaticModeChunkDataPacket::chunkX,
            ByteBufCodecs.VAR_INT,
            UpdateStaticModeChunkDataPacket::chunkZ,
            ByteBufCodecs.<ByteBuf, BlockPos>list().apply(BlockPos.STREAM_CODEC),
            UpdateStaticModeChunkDataPacket::blockPosList,
            UpdateStaticModeChunkDataPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
            AccessControlClient.INSTANCE.updateStaticModeData(chunkPos, blockPosList);
        });
    }
}
