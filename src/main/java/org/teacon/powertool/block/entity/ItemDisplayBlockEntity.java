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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemDisplayBlockEntity extends BlockEntity {

    public ItemStack itemToDisplay = ItemStack.EMPTY;
    public int rotation = 0;

    public ItemDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(PowerToolBlocks.ITEM_DISPLAY_BLOCK_ENTITY.get(), pos, state);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("item", this.itemToDisplay.saveOptional(registries));
        tag.putInt("rotation", this.rotation);
        super.saveAdditional(tag, registries);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.itemToDisplay = ItemStack.parseOptional(registries,tag.getCompound("item"));
        this.rotation = tag.getInt("rotation");
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var result = super.getUpdateTag(registries);
        result.put("item", this.itemToDisplay.saveOptional(registries));
        result.putInt("rotation", this.rotation);
        return result;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.itemToDisplay = ItemStack.parseOptional(registries,tag.getCompound("item"));
        this.rotation = tag.getInt("rotation");
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
        this.handleUpdateTag(pkt.getTag(), lookupProvider);
    }
}
