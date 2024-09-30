package org.teacon.powertool.utils.time;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

//include start and exclude end
public record TimestampTimeSection(long start, long end) implements ITimeSection {
    
    @Override
    public boolean inTimeSection(long timeWithMills) {
        return timeWithMills >= start && timeWithMills < end;
    }
    
    @Override
    //并不能不去每tick检测 因为游戏可以暂停 2个tick间的时长是任意的
    public int nextCheckDelay(long timeWithMills) {
        return 0;
    }
    
    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("start", start);
        tag.putLong("end", end);
    }
    
    @Override
    public ITimeSection load(CompoundTag tag, HolderLookup.Provider registries) {
        var _start = tag.contains("start", Tag.TAG_LONG) ? tag.getLong("start") : 0L;
        var _end = tag.contains("end", Tag.TAG_LONG) ? tag.getLong("end") : 0L;
        return new TimestampTimeSection(_start, _end);
    }
}
