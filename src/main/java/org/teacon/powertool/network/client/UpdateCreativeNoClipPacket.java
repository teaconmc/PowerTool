package org.teacon.powertool.network.client;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.client.CreativeNoClipClient;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record UpdateCreativeNoClipPacket(boolean enabled) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<UpdateCreativeNoClipPacket> TYPE = new Type<>(VanillaUtils.modRL("update_creative_no_clip"));
    
    public static final StreamCodec<ByteBuf, UpdateCreativeNoClipPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UpdateCreativeNoClipPacket::enabled,
            UpdateCreativeNoClipPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> CreativeNoClipClient.setEnabled(this.enabled));
    }
}
