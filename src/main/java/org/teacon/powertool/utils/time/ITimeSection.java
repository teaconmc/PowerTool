package org.teacon.powertool.utils.time;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface ITimeSection {
    
    boolean inTimeSection(long timeWithMills);
    
    int nextCheckDelay(long timeWithMills);
    
    void save(CompoundTag tag, HolderLookup.Provider registries);
    
    ITimeSection load(CompoundTag tag, HolderLookup.Provider registries);
    
    default boolean currentInTimeSection(){
        return inTimeSection(System.currentTimeMillis());
    }
    
    default int nextCheckDelay(){
        return nextCheckDelay(System.currentTimeMillis());
    }
}
