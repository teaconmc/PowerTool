package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.PowerSupplyBlock;
import org.teacon.powertool.block.PowerToolBlocks;

@NonNullByDefault
public final class PowerSupplyBlockEntity extends BlockEntity {
    
    private final EnergyHandler energyHandler = new SimpleEnergyHandler(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public int extract(int amount, TransactionContext transaction) {
            return PowerSupplyBlockEntity.this.data.status == 1 ? Math.min(amount, PowerSupplyBlockEntity.this.data.power) : 0;
        }
        
        @Override
        public int insert(int amount, TransactionContext transaction) {
            return 0;
        }
        
        @Override
        public long getAmountAsLong() {
            return Integer.MAX_VALUE;
        }
    };
    
    public final PowerSupplyBlock.Data data = new PowerSupplyBlock.Data();
    
    public PowerSupplyBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(PowerToolBlocks.POWER_SUPPLY_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        this.data.markDirty = this::setChanged;
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.data.status = input.getIntOr("status", 0);
        this.data.power = input.getIntOr("power", 0);
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("status", this.data.status);
        output.putInt("power", this.data.power);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public EnergyHandler getEnergyStore() {
        return this.energyHandler;
    }
}
