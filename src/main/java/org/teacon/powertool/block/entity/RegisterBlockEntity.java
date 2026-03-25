package org.teacon.powertool.block.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegisterBlockEntity extends BlockEntity implements IClientUpdateBlockEntity{

    public ItemStack itemToAccept = ItemStack.EMPTY;
    public ItemStack itemToSupply = ItemStack.EMPTY;

    public boolean matchDataComponents = false;
    public boolean displaySupply = true;

    public final Container menuView = new Container() {

        @Override
        public void clearContent() {

        }

        @Override
        public int getContainerSize() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot == 0 ? itemToAccept : itemToSupply;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if(slot == 0) itemToAccept = ItemStack.EMPTY;
            if(slot == 1) itemToSupply = ItemStack.EMPTY;
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if(slot == 0) itemToAccept = ItemStack.EMPTY;
            if(slot == 1) itemToSupply = ItemStack.EMPTY;
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if(slot == 0) itemToAccept = stack.copy();
            if(slot == 1) itemToSupply = stack.copy();
        }

        @Override
        public void setChanged() {

        }

        @Override
        public boolean stillValid(Player player) {
            return player.getAbilities().instabuild;
        }
    };

    public RegisterBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.REGISTER_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("item", this.itemToAccept.saveOptional(registries));
        tag.put("itemSupply", this.itemToSupply.saveOptional(registries));
        tag.putBoolean("matchDataComponents", this.matchDataComponents);
        tag.putBoolean("displaySupply", this.displaySupply);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.itemToAccept = ItemStack.parseOptional(registries,tag.getCompound("item"));
        this.itemToSupply = ItemStack.parseOptional(registries,tag.getCompound("itemSupply"));
        this.matchDataComponents = tag.getBoolean("matchDataComponents");
        this.displaySupply = tag.getBoolean("displaySupply");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var result = super.getUpdateTag(registries);
        result.put("item", this.itemToAccept.saveOptional(registries));
        result.put("itemSupply", this.itemToSupply.saveOptional(registries));
        result.putBoolean("matchDataComponents", this.matchDataComponents);
        result.putBoolean("displaySupply", this.displaySupply);
        return result;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.itemToAccept = ItemStack.parseOptional(registries,tag.getCompound("item"));
        this.itemToSupply = ItemStack.parseOptional(registries,tag.getCompound("itemSupply"));
        this.matchDataComponents = tag.getBoolean("matchDataComponents");
        this.displaySupply = tag.getBoolean("displaySupply");
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
    
    @Override
    public void update(CompoundTag tag, HolderLookup.Provider registries) {
        this.matchDataComponents = tag.getBoolean("matchDataComponents");
        this.displaySupply = tag.getBoolean("displaySupply");
    }
    
    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("matchDataComponents", this.matchDataComponents);
        tag.putBoolean("displaySupply", this.displaySupply);
    }
}
