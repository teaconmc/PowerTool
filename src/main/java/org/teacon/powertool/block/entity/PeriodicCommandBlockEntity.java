package org.teacon.powertool.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PeriodicCommandBlockEntity extends CommandBlockEntity {

    private int period = 10;

    public PeriodicCommandBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("period", period);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        period = tag.getInt("period");
        super.loadAdditional(tag, registries);
    }

    @Override
    public BlockEntityType<?> getType() {
        return PowerToolBlocks.COMMAND_BLOCK_ENTITY.get();
    }
}
