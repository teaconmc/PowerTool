package org.teacon.powertool.network.client;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record UpdatePlayerMovement(Operation operation, double x, double y, double z) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdatePlayerMovement> TYPE = new Type<>(VanillaUtils.modRL("update_player_movement"));

    public static final StreamCodec<FriendlyByteBuf, UpdatePlayerMovement> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(Operation.class), UpdatePlayerMovement::operation,
            ByteBufCodecs.DOUBLE, UpdatePlayerMovement::x,
            ByteBufCodecs.DOUBLE, UpdatePlayerMovement::y,
            ByteBufCodecs.DOUBLE, UpdatePlayerMovement::z,
            UpdatePlayerMovement::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context){
        context.enqueueWork(() -> {
            var player = context.player();
            switch (operation) {
                case ADD -> player.addDeltaMovement(new Vec3(x, y, z));
                case SET -> player.setDeltaMovement(x, y, z);
                case MULTIPLY -> player.setDeltaMovement(player.getDeltaMovement().multiply(x, y, z));
            }
        });
    }

    public enum Operation {
        SET,
        ADD,
        MULTIPLY,
    }
}
