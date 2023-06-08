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

    private BlockPos location;
    private String[] messages;

    public UpdateHolographicSignData(BlockPos location, String[] messages) {
        this.location = location;
        this.messages = messages;
        if (messages.length > 4) {
            this.messages = Arrays.copyOfRange(messages, 0, 4);
        }
    }

    public UpdateHolographicSignData(FriendlyByteBuf buf) {
        this.location = buf.readBlockPos();
        var len = buf.readVarInt();
        if (len > 4) {
            len = 4;
        }
        this.messages = new String[len];
        for (int i = 0; i < len; i++) {
            this.messages[i] = buf.readUtf();
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.location);
        buf.writeVarInt(this.messages.length);
        for (var text : this.messages) {
            buf.writeUtf(text);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        var context = contextSupplier.get();
        context.enqueueWork(() -> {
            var sender = context.getSender();
            if (sender != null && sender.getAbilities().instabuild) {
                var level = sender.level();
                if (level.isLoaded(this.location)) {
                    if (level.getBlockEntity(this.location) instanceof HolographicSignBlockEntity theSign) {
                        var state = level.getBlockState(this.location);
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
