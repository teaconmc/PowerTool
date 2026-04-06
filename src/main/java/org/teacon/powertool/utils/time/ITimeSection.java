package org.teacon.powertool.utils.time;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ITimeSection {
    
    boolean inTimeSection(long timeWithMills);
    
    int nextCheckDelay(long timeWithMills);
    
    void save(ValueOutput output);
    
    ITimeSection load(ValueInput input);
    
    default boolean currentInTimeSection() {
        return inTimeSection(System.currentTimeMillis());
    }
    
    default int nextCheckDelay() {
        return nextCheckDelay(System.currentTimeMillis());
    }
}
