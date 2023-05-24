package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.PowerToolBlocks;

public class ItemSupplierBlockEntity extends BlockEntity {

    public ItemStack theItem = ItemStack.EMPTY;

    final LazyOptional<IItemHandler> fakeInv = LazyOptional.of(() -> new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return theItem;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack copy = theItem.copy();
            copy.setCount(amount);
            return copy;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    });

    public ItemSupplierBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.ITEM_SUPPLIER_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("item", this.theItem.save(new CompoundTag()));
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.theItem = ItemStack.of(tag.getCompound("item"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        tag.put("item", this.theItem.save(new CompoundTag()));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.theItem = ItemStack.of(tag.getCompound("item"));
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

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction dir) {
        return cap == ForgeCapabilities.ITEM_HANDLER ? this.fakeInv.cast() : super.getCapability(cap, dir);
    }

    @Override
    public void invalidateCaps() {
        this.fakeInv.invalidate();
        super.invalidateCaps();
    }
}
