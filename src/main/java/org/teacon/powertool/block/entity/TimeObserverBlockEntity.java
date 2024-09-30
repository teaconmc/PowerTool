package org.teacon.powertool.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
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
        if(this.type == null) this.type = type;
    }
    
    public TimeObserverBlock.Type getBlockType(){
        return this.type;
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        readFrom(tag, registries);
        super.loadAdditional(tag, registries);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeTo(tag, registries);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = super.getUpdateTag(registries);
        writeTo(tag, registries);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        readFrom(tag, registries);
        super.handleUpdateTag(tag, registries);
    }
    
    protected void writeTo(CompoundTag tag, HolderLookup.Provider registries) {
        if (type != null) {
            tag.putInt("type", type.ordinal());
            tag.put("timeSection",type.write(timeSection, registries));
        }
    }
    
    protected void readFrom(CompoundTag tag, HolderLookup.Provider registries) {
        if(tag.contains("type", Tag.TAG_INT)) {
            var index = tag.getInt("type");
            if(index >= 0 && index < TimeObserverBlock.Type.values().length) {
                type = TimeObserverBlock.Type.values()[index];
                timeSection = type.readFromTE(this,tag.getCompound("timeSection"), registries);
            }
        }
        this.resetDelay();
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
    
    public static void tick(Level level, BlockPos pos, BlockState state, TimeObserverBlockEntity te) {
        if(te.type == null || te.timeSection == null) return;
        if(te.delay > 0){
            te.delay-=1;
            return;
        }
        var old = state.getValue(TimeObserverBlock.POWERED);
        if(state.getValue(TimeObserverBlock.INVALIDATED)) state.setValue(TimeObserverBlock.POWERED, false);
        else state = state.setValue(TimeObserverBlock.POWERED,te.timeSection.currentInTimeSection());
        te.delay = te.timeSection.nextCheckDelay();
        if(state.getValue(TimeObserverBlock.POWERED) != old && state.getBlock() instanceof TimeObserverBlock block) {
            level.setBlock(pos,state, Block.UPDATE_ALL);
            block.updateNeighborsInFront(level,pos,state);
        }
        
    }
    
    public void resetDelay(){
        this.delay = 0;
    }
    
    @Nullable
    public ITimeSection getTimeSection() {
        return timeSection;
    }
    
    public void setTimeSection(ITimeSection section){
        if(type != null && type.checkType(section)) this.timeSection = section;
    }
    
    @Override
    public void update(CompoundTag tag, HolderLookup.Provider registries) {
        readFrom(tag, registries);
    }
    
    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider registries) {
        writeTo(tag, registries);
    }
}
