package org.teacon.powertool.network.client;

import io.netty.buffer.ByteBuf;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.client.overlay.ClientDebugCharts;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record RecordDebugData(String id, long data) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<RecordDebugData> TYPE = new Type<>(VanillaUtils.modRL("record_debug_data"));
   
    public static final StreamCodec<ByteBuf, RecordDebugData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            RecordDebugData::id,
            ByteBufCodecs.VAR_LONG,
            RecordDebugData::data,
            RecordDebugData::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(IPayloadContext context){
        context.enqueueWork(() -> Handler.run(this));
    }
    
    public static class Handler{
        public static void run(RecordDebugData pack) {
            ClientDebugCharts.recordDebugData(pack.id(),pack.data());
        }
    }
}
