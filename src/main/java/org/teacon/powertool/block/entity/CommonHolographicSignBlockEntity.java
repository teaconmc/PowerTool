package org.teacon.powertool.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommonHolographicSignBlockEntity extends BaseHolographicSignBlockEntity{
    
    public List<? extends Component> contents = Collections.emptyList();
    
    public CommonHolographicSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }
    
    @Override
    public void readFrom(CompoundTag tag, HolderLookup.Provider registries) {
        var loaded = new ArrayList<Component>();
        for (var entry : tag.getList("content", Tag.TAG_STRING)) {
            loaded.add(Component.Serializer.fromJson(entry.getAsString(),registries));
        }
        this.contents = loaded;
        super.readFrom(tag, registries);
        
    }
    
    @Override
    public void writeTo(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        for (var text : this.contents) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(text,registries)));
        }
        tag.put("content", list);
        super.writeTo(tag, registries);
    }
    
    @Override
    public void filterMessage(ServerPlayer player) {
        var task = player.getTextFilter()
                .processMessageBundle(this.contents.stream().map(Component::getString).toList());
        task.thenAcceptAsync(filtered -> {
            if (player.isTextFilteringEnabled()) {
                this.contents = filtered.stream().map(t -> Component.literal(t.filteredOrEmpty())).toList();
            } else {
                this.contents = filtered.stream().map(t -> Component.literal(t.raw())).toList();
            }
            this.setChanged();
            if (level != null) {
                var state = this.getBlockState();
                level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            }
        }, player.server);
    }
}
