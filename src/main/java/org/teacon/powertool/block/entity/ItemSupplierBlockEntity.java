package org.teacon.powertool.block.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemSupplierBlockEntity extends BlockEntity{

    public ItemStack theItem = ItemStack.EMPTY;
    
    private final ResourceHandler<ItemResource> itemHandler = new ResourceHandler<>() {
        @Override
        public int size() {
            return 1;
        }
        
        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(theItem);
        }
        
        @Override
        public long getAmountAsLong(int index) {
            return 64;
        }
        
        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return 64;
        }
        
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(theItem.getItem());
        }
        
        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
        
        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return amount;
        }
    };
    
    public ItemSupplierBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(PowerToolBlocks.ITEM_SUPPLIER_BLOCK_ENTITY.get(), pPos, pBlockState);
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        this.theItem = input.read("item",ItemStack.CODEC).orElse(ItemStack.EMPTY);
        super.loadAdditional(input);
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("item", ItemStack.CODEC, this.theItem);
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
    
    public ResourceHandler<ItemResource> getItemHandler() {
        return this.itemHandler;
    }
    
}
