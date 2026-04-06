package org.teacon.powertool.network.client;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.client.AccessControlClient;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.List;

@MethodsReturnNonnullByDefault
public record UpdateDisplayChunkDataPacket(
        int chunkX,
        int chunkZ,
        List<BlockPos> blockPosList
) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<UpdateDisplayChunkDataPacket> TYPE = new CustomPacketPayload.Type<>(VanillaUtils.modRL("display_chunk_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDisplayChunkDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateDisplayChunkDataPacket::chunkX,
            ByteBufCodecs.VAR_INT,
            UpdateDisplayChunkDataPacket::chunkZ,
            ByteBufCodecs.<ByteBuf, BlockPos>list().apply(BlockPos.STREAM_CODEC),
            UpdateDisplayChunkDataPacket::blockPosList,
            UpdateDisplayChunkDataPacket::new
    );
    
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
            AccessControlClient.INSTANCE.updateDisplayModeData(chunkPos, blockPosList);
        });
    }
}
