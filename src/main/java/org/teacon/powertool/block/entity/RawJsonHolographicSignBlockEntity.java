package org.teacon.powertool.block.entity;

import com.mojang.brigadier.StringReader;
import net.minecraft.commands.ParserUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RawJsonHolographicSignBlockEntity extends BaseHolographicSignBlockEntity{
    
    public String content = "";
    
    public Component forFilter = Component.empty();
    public Component forRender = Component.empty();
    
    public RawJsonHolographicSignBlockEntity( BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.RAW_JSON_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }
    
    @Override
    public void writeTo(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("content",content);
        tag.putString("forRender",Component.Serializer.toJson(forRender,registries));
        super.writeTo(tag, registries);
    }
    
    @Override
    public void readFrom(CompoundTag tag, HolderLookup.Provider registries) {
        content = tag.getString("content");
        forRender = Component.Serializer.fromJson(tag.getString("forRender"),registries);
        try {
            forFilter = ParserUtils.parseJson(registries,new StringReader(content), ComponentSerialization.CODEC);
        }catch (Exception ignore){
        }
        super.readFrom(tag, registries);
    }
    
    @Override
    public void filterMessage(ServerPlayer player) {
        var task = player.getTextFilter()
                .processStreamMessage(forFilter.getString());
        task.thenAcceptAsync(filtered -> {
            if (player.isTextFilteringEnabled()) {
                this.forRender = Component.literal(filtered.filteredOrEmpty()).withStyle(forFilter.getStyle());
            } else {
                this.forRender = forFilter;
            }
            this.setChanged();
            if (level != null) {
                var state = this.getBlockState();
                level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            }
        }, player.server);
    }
}
