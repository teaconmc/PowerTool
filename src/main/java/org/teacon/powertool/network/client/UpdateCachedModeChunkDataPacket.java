package org.teacon.powertool.network.client;

import io.netty.buffer.ByteBuf;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.client.AccessControlClient;
import org.teacon.powertool.client.CachedModeClient;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record UpdateCachedModeChunkDataPacket(
    int chunkX,
    int chunkZ,
    List<BlockPos> blockPosList
) implements CustomPacketPayload {

    public static final Type<UpdateCachedModeChunkDataPacket> TYPE = new Type<>(VanillaUtils.modRL("cached_mode_chunk_data"));

    public static final StreamCodec<ByteBuf, UpdateCachedModeChunkDataPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        UpdateCachedModeChunkDataPacket::chunkX,
        ByteBufCodecs.VAR_INT,
        UpdateCachedModeChunkDataPacket::chunkZ,
        ByteBufCodecs.<ByteBuf, BlockPos>list().apply(BlockPos.STREAM_CODEC),
        UpdateCachedModeChunkDataPacket::blockPosList,
        UpdateCachedModeChunkDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
            CachedModeClient.INSTANCE.updateCachedModeData(chunkPos, blockPosList);
        });
    }
}
