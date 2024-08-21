package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.teacon.powertool.block.PowerSupplyBlock;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PowerSupplyBlockEntity extends BlockEntity {

    private final IEnergyStorage energyStore = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return PowerSupplyBlockEntity.this.data.status == 1 ? PowerSupplyBlockEntity.this.data.power : 0;
        }

        @Override
        public int getEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public final PowerSupplyBlock.Data data = new PowerSupplyBlock.Data();

    public PowerSupplyBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(PowerToolBlocks.POWER_SUPPLY_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        this.data.markDirty = this::setChanged;
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.data.status = tag.getInt("status");
        this.data.power = tag.getInt("power");
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("status", this.data.status);
        tag.putInt("power", this.data.power);
        super.saveAdditional(tag, registries);
    }
    
    public IEnergyStorage getEnergyStore() {
        return this.energyStore;
    }
}
