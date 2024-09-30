package org.teacon.powertool.utils.time;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class InWorldDailyCycleTimeSection implements ITimeSection{
    
    protected final Supplier<Level> levelGetter;
    //[0.0-1.0]
    protected final float start;
    protected final float end;
    
    public InWorldDailyCycleTimeSection(Supplier<Level> levelGetter, float start, float end) {
        this.levelGetter = levelGetter;
        this.start = start;
        this.end = end;
    }
    
    @Override
    public boolean inTimeSection(long timeWithMills) {
        var current = levelGetter.get().getTimeOfDay(0f);
        return start <= current && end >= current;
    }
    
    @Override
    public int nextCheckDelay(long timeWithMills) {
        return 1;
    }
    
    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putFloat("start", start);
        tag.putFloat("end", end);
    }
    
    @Override
    public ITimeSection load(CompoundTag tag, HolderLookup.Provider registries) {
        var _start = tag.contains("start", Tag.TAG_FLOAT) ? tag.getFloat("start") : 0;
        var _end = tag.contains("end", Tag.TAG_FLOAT) ? tag.getFloat("end") : 0;
        return new InWorldDailyCycleTimeSection(levelGetter, _start, _end);
    }
    
    public float getStart() {
        return start;
    }
    
    public float getEnd() {
        return end;
    }
}
