package org.teacon.powertool.network.server;

import com.mojang.logging.LogUtils;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.teacon.powertool.block.entity.BaseHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.IClientUpdateBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record UpdateBlockEntityData(CompoundTag data, BlockPos location) implements CustomPacketPayload {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Type<UpdateBlockEntityData> TYPE = new Type<>(VanillaUtils.modRL("update_holographic_sign"));
    private static final ProblemReporter.PathElement PATH = new ClientPathElement();
    public static final StreamCodec<ByteBuf, UpdateBlockEntityData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            UpdateBlockEntityData::data,
            BlockPos.STREAM_CODEC,
            UpdateBlockEntityData::location,
            UpdateBlockEntityData::new
    );
    
    public static UpdateBlockEntityData create(BlockEntity entity) {
        var tag = new CompoundTag();
        if (entity instanceof IClientUpdateBlockEntity theTE) {
            var registries = entity.getLevel().registryAccess();
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(PATH, LOGGER)) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
                theTE.writeFromClient(output);
                tag = output.buildResult();
            }
        }
        
        return new UpdateBlockEntityData(tag, entity.getBlockPos());
    }
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var sender = context.player();
            var level = sender.level();
            if (!level.isLoaded(this.location)) return;
            
            var te = level.getBlockEntity(this.location);
            if (te instanceof IClientUpdateBlockEntity theTE) {
                var registries = te.getLevel().registryAccess();
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(PATH, LOGGER)) {
                    var input = TagValueInput.create(reporter, registries, this.data);
                    theTE.updateFromClient(input);
                }
                var state = level.getBlockState(this.location);
                te.setChanged();
                level.sendBlockUpdated(this.location, state, state, Block.UPDATE_CLIENTS);
                if (sender instanceof ServerPlayer serverPlayer && te instanceof BaseHolographicSignBlockEntity theSign) {
                    theSign.filterMessage(serverPlayer);
                }
            }
            
        });
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static class ClientPathElement implements ProblemReporter.PathElement {
        
        @Override
        public String get() {
            return "powertool:client_update";
        }
    }
    
}
