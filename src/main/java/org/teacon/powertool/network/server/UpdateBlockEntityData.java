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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.block.entity.BaseHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.IClientUpdateBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.Objects;

@MethodsReturnNonnullByDefault
public record UpdateBlockEntityData(CompoundTag data, BlockPos location) implements CustomPacketPayload {
    
    public static final Type<UpdateBlockEntityData> TYPE = new Type<>(VanillaUtils.modRL("update_holographic_sign"));
    
    public static final StreamCodec<ByteBuf, UpdateBlockEntityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            UpdateBlockEntityData::data,
            BlockPos.STREAM_CODEC,
            UpdateBlockEntityData::location,
            UpdateBlockEntityData::new
    );
    
    public static UpdateBlockEntityData create(BlockEntity entity) {
        var tag = new CompoundTag();
        if(entity instanceof IClientUpdateBlockEntity theTE) {
            theTE.writeToPacket(tag, Objects.requireNonNull(entity.getLevel()).registryAccess());
        }
        
        return new UpdateBlockEntityData(tag,entity.getBlockPos());
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var sender = context.player();
            var level = sender.level();
            if (!level.isLoaded(this.location)) return;
            
            var te = level.getBlockEntity(this.location);
            if (te instanceof IClientUpdateBlockEntity theTE) {
                theTE.update(this.data,level.registryAccess());
                var state = level.getBlockState(this.location);
                te.setChanged();
                level.sendBlockUpdated(this.location, state, state, Block.UPDATE_CLIENTS);
                if(sender instanceof ServerPlayer serverPlayer && te instanceof BaseHolographicSignBlockEntity theSign) {
                    theSign.filterMessage(serverPlayer);
                }
            }
            
        });
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    

}
