package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.teacon.powertool.block.PowerSupplyBlock;
import org.teacon.powertool.block.PowerToolBlocks;

public final class PowerSupplyBlockEntity extends BlockEntity {

    private final LazyOptional<IEnergyStorage> energyStore = LazyOptional.of(() -> new IEnergyStorage() {
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
    });

    public final PowerSupplyBlock.Data data = new PowerSupplyBlock.Data();

    public PowerSupplyBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(PowerToolBlocks.POWER_SUPPLY_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        this.data.markDirty = this::setChanged;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("status", this.data.status);
        tag.putInt("power", this.data.power);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.data.status = tag.getInt("status");
        this.data.power = tag.getInt("power");
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction d) {
        return cap == ForgeCapabilities.ENERGY ? this.energyStore.cast() : super.getCapability(cap, d);
    }

    @Override
    public void invalidateCaps() {
        this.energyStore.invalidate();
        super.invalidateCaps();
    }
}
