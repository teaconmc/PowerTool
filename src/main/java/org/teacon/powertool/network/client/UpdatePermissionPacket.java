package org.teacon.powertool.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record UpdatePermissionPacket(boolean canUseGameMasterBlock, boolean canSwitchGameMode, boolean canUseSelector) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<UpdatePermissionPacket> TYPE = new Type<>(VanillaUtils.modResourceLocation("update_permission"));

    public static final StreamCodec<ByteBuf,UpdatePermissionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,UpdatePermissionPacket::canUseGameMasterBlock,
            ByteBufCodecs.BOOL,UpdatePermissionPacket::canSwitchGameMode,
            ByteBufCodecs.BOOL,UpdatePermissionPacket::canUseSelector,
            UpdatePermissionPacket::new
    );
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                var permission = minecraft.player.getData(PowerToolAttachments.PERMISSION);
                permission.setCanSwitchGameMode(canSwitchGameMode);
                permission.setCanUseGameMasterBlock(canUseGameMasterBlock);
                permission.setCanUseSelector(canUseSelector);
            }
        });
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
