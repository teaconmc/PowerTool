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
import org.teacon.powertool.block.RedStoneDelayBlock;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RedStoneDelayBlockEntity extends BlockEntity implements IClientUpdateBlockEntity{
    
    public int delayTicks;
    public int delayedTicks;
    public boolean counting;
    public boolean checkRisingEdge;
    public Mode mode = Mode.IGNORE;
    public Boolean powered;
    public Boolean poweredOld;
    
    
    public RedStoneDelayBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.DELAYER_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, RedStoneDelayBlockEntity blockEntity) {
        if(blockEntity.powered == null || blockEntity.poweredOld == null) {
            blockEntity.powered = blockEntity.poweredOld = RedStoneDelayBlock.powered(level, pos);
        }
        Boolean risingEdge = null;
        if(blockEntity.powered != blockEntity.poweredOld){
            risingEdge = blockEntity.powered;
            blockEntity.poweredOld = blockEntity.powered;
        }
        if(risingEdge != null && blockEntity.checkRisingEdge == risingEdge){
            blockEntity.counting = true;
            if(blockEntity.mode == Mode.RESET) blockEntity.delayedTicks = 0;
            blockEntity.setChanged();
        }
        if(blockEntity.counting){
            blockEntity.delayedTicks++;
            if(blockEntity.delayedTicks%2==0) level.updateNeighbourForOutputSignal(pos,state.getBlock());
        }
        if(blockEntity.counting && blockEntity.delayedTicks >= blockEntity.delayTicks){
            blockEntity.delayedTicks = 0;
            blockEntity.counting = false;
            level.setBlock(pos,state.setValue(RedStoneDelayBlock.POWERED, true), Block.UPDATE_ALL);
            level.scheduleTick(pos,state.getBlock(),2);
            blockEntity.setChanged();
        }
        
    }
    
    public void readWithoutState(ValueInput input) {
        this.delayTicks = input.getIntOr("DelayTicks", this.delayTicks);
        this.mode = Mode.fromId(input.getIntOr("Mode", this.mode.ordinal()));
        this.checkRisingEdge = input.getBooleanOr("checkRisingEdge", this.checkRisingEdge);
    }
    
    public void writeWithoutState(ValueOutput output) {
        output.putInt("DelayTicks", this.delayTicks);
        output.putInt("Mode", this.mode.ordinal());
        output.putBoolean("checkRisingEdge", this.checkRisingEdge);
    }
    
    public void read(ValueInput input) {
        this.readWithoutState(input);
        this.delayedTicks = input.getIntOr("DelayedTicks", this.delayedTicks);
        this.counting = input.getBooleanOr("counting", this.counting);
    }
    
    public void write(ValueOutput output) {
        this.writeWithoutState(output);
        output.putInt("DelayedTicks", this.delayedTicks);
        output.putBoolean("counting", this.counting);
        
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.read(input);
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.write(output);
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
        this.readWithoutState(input);
    }
    
    @Override
    public void writeFromClient(ValueOutput output) {
        this.writeWithoutState(output);
    }
    
    public enum Mode{
        IGNORE,
        RESET;
        
        public static Mode fromId(int id) {
            return id == 0 ? IGNORE : RESET;
        }
    }
}
