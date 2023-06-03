package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
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

public class ItemDisplayBlockEntity extends BlockEntity {

    public ItemStack itemToDisplay = ItemStack.EMPTY;
    public int rotation = 0;

    public ItemDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(PowerToolBlocks.ITEM_DISPLAY_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("item", this.itemToDisplay.save(new CompoundTag()));
        tag.putInt("rotation", this.rotation);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.itemToDisplay = ItemStack.of(tag.getCompound("item"));
        this.rotation = tag.getInt("rotation");
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        tag.put("item", this.itemToDisplay.save(new CompoundTag()));
        tag.putInt("rotation", this.rotation);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.itemToDisplay = ItemStack.of(tag.getCompound("item"));
        this.rotation = tag.getInt("rotation");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        this.handleUpdateTag(pkt.getTag());
    }
}
