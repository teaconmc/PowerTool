package org.teacon.powertool.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TempleBlockEntity extends BlockEntity {
    
    public ItemStack theItem = ItemStack.EMPTY;
    
    public TempleBlockEntity( BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.TEMPLE_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        this.theItem = ItemStack.parseOptional(registries,tag.getCompound("item"));
        super.loadAdditional(tag, registries);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("item", this.theItem.saveOptional(registries));
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var result = super.getUpdateTag(registries);
        result.put("item", this.theItem.saveOptional(registries));
        return result;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.theItem = ItemStack.parseOptional(registries,tag.getCompound("item"));
        super.handleUpdateTag(tag, registries);
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        this.handleUpdateTag(pkt.getTag(),lookupProvider);
    }
}
