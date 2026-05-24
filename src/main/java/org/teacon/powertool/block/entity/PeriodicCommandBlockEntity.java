package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.PowerToolBlocks;

@NonNullByDefault
public class PeriodicCommandBlockEntity extends CommandBlockEntity {
    
    private int period = 10;
    
    @SuppressWarnings("deprecation")
    public PeriodicCommandBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        this.type = PowerToolBlocks.COMMAND_BLOCK_ENTITY.get();
    }
    
    public void setPeriod(int period) {
        this.period = Math.max(period, 1);
        this.setChanged();
    }
    
    public int getPeriod() {
        return period;
    }
    
    @Override
    public Mode getMode() {
        var state = this.getBlockState();
        if (state.is(PowerToolBlocks.COMMAND_BLOCK.get())) {
            return Mode.AUTO;
        } else {
            return super.getMode();
        }
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.period = input.getIntOr("period", 10);
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("period", period);
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
    public BlockEntityType<?> getType() {
        return PowerToolBlocks.COMMAND_BLOCK_ENTITY.get();
    }
}
