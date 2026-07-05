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
import org.jspecify.annotations.Nullable;
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.itemToDisplay = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.rotation = input.getIntOr("rotation", 0);
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        if(!this.itemToDisplay.isEmpty()) output.store("item", ItemStack.CODEC, this.itemToDisplay);
        output.putInt("rotation", this.rotation);
        super.saveAdditional(output);
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
    
    
}
