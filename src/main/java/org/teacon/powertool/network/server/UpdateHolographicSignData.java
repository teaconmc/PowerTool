package org.teacon.powertool.network.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;
import org.teacon.powertool.block.entity.HolographicSignBlockEntity;

import java.util.Arrays;
import java.util.function.Supplier;

public class UpdateHolographicSignData {

    public static final int MAXIMUM_LINE_COUNT = 10;

    private BlockPos location;
    private String[] messages;
    private int color;
    private float scale;
    private HolographicSignBlockEntity.Align align;
    private HolographicSignBlockEntity.Shadow shadow;
    private HolographicSignBlockEntity.LayerArrange layerArrange;
    
    private boolean locked;
    private int rotation;
    private boolean bidirectional;
    
    public UpdateHolographicSignData(BlockPos location, String[] messages, int color, float scale,
                                     HolographicSignBlockEntity.Align align,
                                     HolographicSignBlockEntity.Shadow shadow,
                                     HolographicSignBlockEntity.LayerArrange layerArrange,
                                     boolean locked,int rotation,boolean bidirectional) {
        this.location = location;
        this.messages = messages;
        if (messages.length > MAXIMUM_LINE_COUNT) {
            this.messages = Arrays.copyOfRange(messages, 0, MAXIMUM_LINE_COUNT);
        }
        this.color = color;
        this.scale = scale;
        this.align = align;
        this.shadow = shadow;
        this.layerArrange = layerArrange;
        this.locked = locked;
        this.rotation = rotation;
        this.bidirectional = bidirectional;
    }

    public UpdateHolographicSignData(FriendlyByteBuf buf) {
        this.location = buf.readBlockPos();
        var len = buf.readVarInt();
        if (len > MAXIMUM_LINE_COUNT) {
            len = MAXIMUM_LINE_COUNT;
        }
        this.messages = new String[len];
        for (int i = 0; i < len; i++) {
            this.messages[i] = buf.readUtf();
        }
        this.color = buf.readVarInt();
        this.scale = buf.readFloat();
        this.align = buf.readEnum(HolographicSignBlockEntity.Align.class);
        this.shadow = buf.readEnum(HolographicSignBlockEntity.Shadow.class);
        this.layerArrange = buf.readEnum(HolographicSignBlockEntity.LayerArrange.class);
        this.locked = buf.readBoolean();
        this.rotation = buf.readInt();
        this.bidirectional = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.location);
        buf.writeVarInt(this.messages.length);
        for (var text : this.messages) {
            buf.writeUtf(text);
        }
        buf.writeVarInt(this.color);
        buf.writeFloat(this.scale);
        buf.writeEnum(this.align);
        buf.writeEnum(this.shadow);
        buf.writeEnum(this.layerArrange);
        buf.writeBoolean(locked);
        buf.writeInt(rotation);
        buf.writeBoolean(bidirectional);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        context.enqueueWork(() -> {
            var sender = context.getSender();
            if (sender != null && sender.getAbilities().instabuild) {
                var level = sender.level();
                if (level.isLoaded(this.location)) {
                    if (level.getBlockEntity(this.location) instanceof HolographicSignBlockEntity theSign) {
                        theSign.colorInARGB = this.color;
                        theSign.scale = this.scale;
                        theSign.align = this.align;
                        theSign.shadow = this.shadow;
                        theSign.arrange = this.layerArrange;
                        theSign.lock = this.locked;
                        theSign.rotate = this.rotation;
                        theSign.bidirectional = this.bidirectional;
                        var state = level.getBlockState(this.location);
                        theSign.setChanged();
                        level.sendBlockUpdated(this.location, state, state, Block.UPDATE_CLIENTS);
                        var task = sender.getTextFilter().processMessageBundle(Arrays.asList(this.messages));
                        task.thenAcceptAsync(filtered -> {
                            if (sender.isTextFilteringEnabled()) {
                                theSign.contents = filtered.stream().map(t -> Component.literal(t.filtered())).toList();
                            } else {
                                theSign.contents = filtered.stream().map(t -> Component.literal(t.raw())).toList();
                            }
                            theSign.setChanged();
                            level.sendBlockUpdated(this.location, state, state, Block.UPDATE_CLIENTS);
                        }, sender.server);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
