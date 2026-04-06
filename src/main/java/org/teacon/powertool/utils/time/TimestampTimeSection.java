package org.teacon.powertool.utils.time;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void save(ValueOutput output) {
        output.putLong("start", start);
        output.putLong("end", end);
    }
    
    @Override
    public ITimeSection load(ValueInput input) {
        var _start = input.getLong("start").orElse(0L);
        var _end = input.getLong("end").orElse(0L);
        return new TimestampTimeSection(_start, _end);
    }
}
