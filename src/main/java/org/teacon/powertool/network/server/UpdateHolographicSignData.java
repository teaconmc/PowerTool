package org.teacon.powertool.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.block.entity.BaseHolographicSignBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.Objects;

@MethodsReturnNonnullByDefault
public record UpdateHolographicSignData(CompoundTag data,BlockPos location) implements CustomPacketPayload {
    
    public static final Type<UpdateHolographicSignData> TYPE = new Type<>(VanillaUtils.modResourceLocation("update_holographic_sign"));
    
    public static final StreamCodec<ByteBuf, UpdateHolographicSignData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            UpdateHolographicSignData::data,
            BlockPos.STREAM_CODEC,
            UpdateHolographicSignData::location,
            UpdateHolographicSignData::new
    );
    
    public static UpdateHolographicSignData create(BaseHolographicSignBlockEntity entity) {
        var tag = new CompoundTag();
        entity.writeTo(tag, Objects.requireNonNull(entity.getLevel()).registryAccess());
        return new UpdateHolographicSignData(tag,entity.getBlockPos());
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var sender = context.player();
            if (sender.getAbilities().instabuild) {
                var level = sender.level();
                if (!level.isLoaded(this.location)) return;
                if (level.getBlockEntity(this.location) instanceof BaseHolographicSignBlockEntity theSign) {
                    theSign.readFrom(this.data,level.registryAccess());
                    var state = level.getBlockState(this.location);
                    theSign.setChanged();
                    level.sendBlockUpdated(this.location, state, state, Block.UPDATE_CLIENTS);
                    if(sender instanceof ServerPlayer serverPlayer) {
                        theSign.filterMessage(serverPlayer);
                    }
                }
            }
        });
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    

}
