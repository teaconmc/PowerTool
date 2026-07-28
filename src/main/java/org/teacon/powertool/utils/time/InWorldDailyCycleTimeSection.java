package org.teacon.powertool.utils.time;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.timeline.Timelines;

import java.util.function.Supplier;

public class InWorldDailyCycleTimeSection implements ITimeSection {
    
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
        var period = levelGetter.get()
                .holder(Timelines.OVERWORLD_DAY)
                .flatMap(timeLine -> timeLine.getDelegate().value().periodTicks());
        if(period.isPresent()) {
            var current = (double)(levelGetter.get().getOverworldClockTime() % period.get()) / period.get();
            if(start < end) return start <= current && end > current;
            else return start <= current || end > current;
        }
        return false;
    }
    
    @Override
    public int nextCheckDelay(long timeWithMills) {
        return 1;
    }
    
    @Override
    public void save(ValueOutput output) {
        output.putFloat("start", start);
        output.putFloat("end", end);
    }
    
    @Override
    public ITimeSection load(ValueInput input) {
        var _start = input.getFloatOr("start", 0);
        var _end = input.getFloatOr("end", 0);
        return new InWorldDailyCycleTimeSection(levelGetter, _start, _end);
    }
    
    
    public float getStart() {
        return start;
    }
    
    public float getEnd() {
        return end;
    }
}
