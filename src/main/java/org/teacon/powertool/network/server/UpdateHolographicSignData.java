package org.teacon.powertool.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.block.entity.HolographicSignBlockEntity;
import org.teacon.powertool.utils.CodecUtils;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.List;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public record UpdateHolographicSignData(UpdateHolographicSignDataInner data) implements CustomPacketPayload {
    
    
    public static final int MAXIMUM_LINE_COUNT = 10;
    
    public static final Type<UpdateHolographicSignData> TYPE = new Type<>(VanillaUtils.modResourceLocation("update_holographic_sign"));
    
    public static final StreamCodec<ByteBuf, UpdateHolographicSignData> STREAM_CODEC = StreamCodec.composite(
            UpdateHolographicSignDataInner.STREAM_CODEC,
            UpdateHolographicSignData::data,
            UpdateHolographicSignData::new
    );
    
    public static UpdateHolographicSignData create(BlockPos location, List<String> messages, int color, float scale,
                                     HolographicSignBlockEntity.Align align,
                                     HolographicSignBlockEntity.Shadow shadow,
                                     HolographicSignBlockEntity.LayerArrange layerArrange,
                                     boolean locked,int rotation,boolean bidirectional) {
        if (messages.size() > MAXIMUM_LINE_COUNT) {
            messages = messages.stream().limit(10).collect(Collectors.toList());
        }
        return new UpdateHolographicSignData(new UpdateHolographicSignDataInner(location,messages,color,scale,align,shadow,layerArrange,locked, rotation, bidirectional));
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var sender = context.player();
            if (sender.getAbilities().instabuild) {
                var level = sender.level();
                if (level.isLoaded(this.data.location)) {
                    if (level.getBlockEntity(this.data.location) instanceof HolographicSignBlockEntity theSign) {
                        theSign.colorInARGB = this.data.color;
                        theSign.scale = this.data.scale;
                        theSign.align = this.data.align;
                        theSign.shadow = this.data.shadow;
                        theSign.arrange = this.data.layerArrange;
                        theSign.lock = this.data.locked;
                        theSign.rotate = this.data.rotation;
                        theSign.bidirectional = this.data.bidirectional;
                        var state = level.getBlockState(this.data.location);
                        theSign.setChanged();
                        level.sendBlockUpdated(this.data.location, state, state, Block.UPDATE_CLIENTS);
                        if(sender instanceof ServerPlayer serverPlayer) {
                            var task = serverPlayer.getTextFilter().processMessageBundle(this.data.messages);
                            task.thenAcceptAsync(filtered -> {
                                if (sender.isTextFilteringEnabled()) {
                                    theSign.contents = filtered.stream().map(t -> Component.literal(t.filtered())).toList();
                                } else {
                                    theSign.contents = filtered.stream().map(t -> Component.literal(t.raw())).toList();
                                }
                                theSign.setChanged();
                                level.sendBlockUpdated(this.data.location, state, state, Block.UPDATE_CLIENTS);
                            }, serverPlayer.server);
                        }
                       
                    }
                }
            }
        });
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    private record UpdateHolographicSignDataInner(BlockPos location, List<String> messages, int color, float scale,
                                                  HolographicSignBlockEntity.Align align,
                                                  HolographicSignBlockEntity.Shadow shadow,
                                                  HolographicSignBlockEntity.LayerArrange layerArrange,
                                                  boolean locked, int rotation, boolean bidirectional){
        
        public static final StreamCodec<ByteBuf, UpdateHolographicSignDataInner> STREAM_CODEC = CodecUtils.composite10(
                BlockPos.STREAM_CODEC,
                UpdateHolographicSignDataInner::location,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                UpdateHolographicSignDataInner::messages,
                ByteBufCodecs.INT,
                UpdateHolographicSignDataInner::color,
                ByteBufCodecs.FLOAT,
                UpdateHolographicSignDataInner::scale,
                HolographicSignBlockEntity.Align.STREAM_CODEC,
                UpdateHolographicSignDataInner::align,
                HolographicSignBlockEntity.Shadow.STREAM_CODEC,
                UpdateHolographicSignDataInner::shadow,
                HolographicSignBlockEntity.LayerArrange.STREAM_CODEC,
                UpdateHolographicSignDataInner::layerArrange,
                ByteBufCodecs.BOOL,
                UpdateHolographicSignDataInner::locked,
                ByteBufCodecs.VAR_INT,
                UpdateHolographicSignDataInner::rotation,
                ByteBufCodecs.BOOL,
                UpdateHolographicSignDataInner::bidirectional,
                UpdateHolographicSignDataInner::new
        );
        
    }
}
