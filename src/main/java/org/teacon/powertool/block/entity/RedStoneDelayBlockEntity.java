package org.teacon.powertool.block.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
    
    public void readWithOutState(CompoundTag tag) {
        if(tag.contains("DelayTicks")) this.delayTicks = tag.getInt("DelayTicks");
        if(tag.contains("Mode")) this.mode = Mode.fromId(tag.getInt("Mode"));
        if(tag.contains("checkRisingEdge")) this.checkRisingEdge = tag.getBoolean("checkRisingEdge");
    }
    
    public void read(CompoundTag tag) {
        readWithOutState(tag);
        if(tag.contains("DelayedTicks")) this.delayedTicks = tag.getInt("DelayedTicks");
        if(tag.contains("counting")) this.counting = tag.getBoolean("counting");
    }
    
    public CompoundTag write(CompoundTag tag) {
        tag.putInt("DelayTicks", this.delayTicks);
        tag.putInt("DelayedTicks", this.delayedTicks);
        tag.putInt("Mode", this.mode.ordinal());
        tag.putBoolean("counting", this.counting);
        tag.putBoolean("checkRisingEdge", this.checkRisingEdge);
        return tag;
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        read(tag);
        super.loadAdditional(tag, registries);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        write(tag);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var result = super.getUpdateTag(registries);
        return write(result);
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        read(tag);
        super.handleUpdateTag(tag, registries);
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        this.handleUpdateTag(pkt.getTag(),lookupProvider);
    }
    
    @Override
    public void update(CompoundTag tag, HolderLookup.Provider registries) {
        readWithOutState(tag);
    }
    
    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider registries) {
        write(tag);
    }
    
    public enum Mode{
        IGNORE,
        RESET;
        
        public static Mode fromId(int id) {
            return id == 0 ? IGNORE : RESET;
        }
    }
}
