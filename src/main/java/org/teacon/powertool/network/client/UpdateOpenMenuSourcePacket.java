package org.teacon.powertool.network.client;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.client.AccessControlClient;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record UpdateOpenMenuSourcePacket(
    BlockPos pos
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateOpenMenuSourcePacket> TYPE = new Type<>(VanillaUtils.modRL("open_menu_source"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateOpenMenuSourcePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        UpdateOpenMenuSourcePacket::pos,
        UpdateOpenMenuSourcePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context){
        AccessControlClient.INSTANCE.updateInteractionSource(pos);
    }
}
