package org.teacon.powertool.block.entity;

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
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.PowerToolBlocks;

@NonNullByDefault
public class TempleBlockEntity extends BlockEntity {
    
    public ItemStack theItem = ItemStack.EMPTY;
    
    public TempleBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.TEMPLE_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.theItem = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if(!this.theItem.isEmpty()) output.store("item", ItemStack.CODEC, this.theItem);
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
