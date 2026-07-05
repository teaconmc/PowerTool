package org.teacon.powertool.network.client;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.client.FlyNoDriftClient;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record UpdateFlyNoDriftPacket(boolean enabled) implements CustomPacketPayload {
    
    public static final Type<UpdateFlyNoDriftPacket> TYPE = new Type<>(VanillaUtils.modRL("update_fly_no_drift"));
    
    public static final StreamCodec<ByteBuf, UpdateFlyNoDriftPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UpdateFlyNoDriftPacket::enabled,
            UpdateFlyNoDriftPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> FlyNoDriftClient.setEnabled(this.enabled));
    }
}
