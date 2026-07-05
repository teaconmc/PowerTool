package org.teacon.powertool.block.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegisterBlockEntity extends BlockEntity implements IClientUpdateBlockEntity {
    
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
            if (slot == 0) itemToAccept = ItemStack.EMPTY;
            if (slot == 1) itemToSupply = ItemStack.EMPTY;
            return ItemStack.EMPTY;
        }
        
        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if (slot == 0) itemToAccept = ItemStack.EMPTY;
            if (slot == 1) itemToSupply = ItemStack.EMPTY;
            return ItemStack.EMPTY;
        }
        
        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot == 0) itemToAccept = stack.copy();
            if (slot == 1) itemToSupply = stack.copy();
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if(!this.itemToAccept.isEmpty()) output.store("item", ItemStack.CODEC, this.itemToAccept);
        if(!this.itemToSupply.isEmpty()) output.store("itemSupply", ItemStack.CODEC, this.itemToSupply);
        output.putBoolean("matchDataComponents", this.matchDataComponents);
        output.putBoolean("displaySupply", this.displaySupply);
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.itemToAccept = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.itemToSupply = input.read("itemSupply", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.matchDataComponents = input.getBooleanOr("matchDataComponents", false);
        this.displaySupply = input.getBooleanOr("displaySupply", true);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public void updateFromClient(ValueInput input) {
        this.matchDataComponents = input.getBooleanOr("matchDataComponents", this.matchDataComponents);
        this.displaySupply = input.getBooleanOr("displaySupply", this.displaySupply);
    }
    
    @Override
    public void writeFromClient(ValueOutput output) {
        output.putBoolean("matchDataComponents", this.matchDataComponents);
        output.putBoolean("displaySupply", this.displaySupply);
    }
}
