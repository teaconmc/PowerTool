package org.teacon.powertool.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.client.renders.BezierCurveBlockRenderer;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.List;

@NonNullByDefault
public record UpdateBezierCurveChunkDataPacket(
        int chunkX,
        int chunkZ,
        List<BlockPos> blockPosList
) implements CustomPacketPayload {

    public static final Type<UpdateBezierCurveChunkDataPacket> TYPE = new Type<>(VanillaUtils.modRL("bezier_curve_chunk_data"));
    public static final StreamCodec<ByteBuf, UpdateBezierCurveChunkDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateBezierCurveChunkDataPacket::chunkX,
            ByteBufCodecs.VAR_INT,
            UpdateBezierCurveChunkDataPacket::chunkZ,
            ByteBufCodecs.<ByteBuf, BlockPos>list().apply(BlockPos.STREAM_CODEC),
            UpdateBezierCurveChunkDataPacket::blockPosList,
            UpdateBezierCurveChunkDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> BezierCurveBlockRenderer.updateChunk(chunkX, chunkZ, blockPosList));
    }
}
