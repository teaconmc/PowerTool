package org.teacon.powertool.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class LinkHolographicSignBlockEntity extends BaseHolographicSignBlockEntity{
    
    public Component displayContent = Component.empty();
    public String url = "";
    
    public LinkHolographicSignBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.LINK_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.colorInARGB =  Objects.requireNonNullElse(ChatFormatting.BLUE.getColor(), VanillaUtils.getColor(255,255,255,255));
    }
    
    @Override
    public void writeTo(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("url", url);
        tag.putString("content",Component.Serializer.toJson(displayContent,registries));
        super.writeTo(tag, registries);
    }
    
    @Override
    public void readFrom(CompoundTag tag, HolderLookup.Provider registries) {
        url = tag.getString("url");
        displayContent = Component.Serializer.fromJson(tag.getString("content"),registries);
        super.readFrom(tag, registries);
    }
    
    @Override
    public void filterMessage(ServerPlayer player) {
        var task = player.getTextFilter()
                .processStreamMessage(displayContent.getString());
        task.thenAcceptAsync(filtered -> {
            if (player.isTextFilteringEnabled()) {
                this.displayContent = Component.literal(filtered.filteredOrEmpty());
            } else {
                this.displayContent = Component.literal(filtered.raw());
            }
            this.setChanged();
            if (level != null) {
                var state = this.getBlockState();
                level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            }
        }, player.server);
    }
}
