package org.teacon.powertool.block.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.TimeObserverBlock;
import org.teacon.powertool.utils.time.ITimeSection;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TimeObserverBlockEntity extends BlockEntity implements IClientUpdateBlockEntity {
    
    private TimeObserverBlock.Type type;
    private ITimeSection timeSection;
    private int delay = 0;
    
    public TimeObserverBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.TIME_OBSERVER_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    public void setType(TimeObserverBlock.Type type) {
        if (this.type == null) this.type = type;
    }
    
    public TimeObserverBlock.Type getBlockType() {
        return this.type;
    }
    
    protected void writeTo(ValueOutput output) {
        if (type != null) {
            output.putInt("type", type.ordinal());
            if (this.timeSection != null) timeSection.save(output);
        }
    }
    
    protected void readFrom(ValueInput input) {
        var index = input.getInt("type");
        if (index.isPresent()) {
            var i = index.get();
            if (i >= 0 && i < TimeObserverBlock.Type.values().length) {
                type = TimeObserverBlock.Type.values()[i];
                timeSection = type.readFromTE(this, input);
            }
        } else {
            this.type = null;
        }
        this.resetDelay();
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.readFrom(input);
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.writeTo(output);
    }
    
    @Override
    public void writeFromClient(ValueOutput output) {
        this.writeTo(output);
    }
    
    @Override
    public void updateFromClient(ValueInput input) {
        this.readFrom(input);
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
    
    
    public static void tick(Level level, BlockPos pos, BlockState state, TimeObserverBlockEntity te) {
        if (te.type == null || te.timeSection == null) return;
        if (te.delay > 0) {
            te.delay -= 1;
            return;
        }
        var old = state.getValue(TimeObserverBlock.POWERED);
        if (state.getValue(TimeObserverBlock.INVALIDATED)) state.setValue(TimeObserverBlock.POWERED, false);
        else state = state.setValue(TimeObserverBlock.POWERED, te.timeSection.currentInTimeSection());
        te.delay = te.timeSection.nextCheckDelay();
        if (state.getValue(TimeObserverBlock.POWERED) != old && state.getBlock() instanceof TimeObserverBlock block) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
            block.updateNeighborsInFront(level, pos, state);
        }
        
    }
    
    public void resetDelay() {
        this.delay = 0;
    }
    
    @Nullable
    public ITimeSection getTimeSection() {
        return timeSection;
    }
    
    public void setTimeSection(ITimeSection section) {
        if (type != null && type.checkType(section)) this.timeSection = section;
    }
}
