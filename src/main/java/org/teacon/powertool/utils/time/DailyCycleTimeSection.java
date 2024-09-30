package org.teacon.powertool.utils.time;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

//todo 按小时算的offset貌似不够合理 另外夏令时也没处理(夏令时真可恨吧) -- xkball
public class DailyCycleTimeSection implements ITimeSection {

    protected final int start;
    protected final int end;
    protected final int hourOffset;
    protected final LocalTime localStart;
    protected final LocalTime localEnd;
    protected final ZoneOffset zoneOffset;
    
    //format: HHmmssSSS | offset: relative to UTC
    public DailyCycleTimeSection(int start, int end, int hourOffset) {
        assert start <= end;
        assert -18 <= hourOffset && hourOffset <= 18;
        this.start = start;
        this.end = end;
        this.hourOffset = hourOffset;
        this.localStart = fromFormatedInt(start);
        this.localEnd = fromFormatedInt(end);
        this.zoneOffset = ZoneOffset.ofHours(hourOffset);
    }
    

    
    @Override
    public boolean inTimeSection(long timeWithMills) {
        var t = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeWithMills), zoneOffset).toLocalTime();
        return t.isAfter(localStart) && t.isBefore(localEnd);
    }
    
    @Override
    //并不能不去每tick检测 因为游戏可以暂停 2个tick间的时长是任意的
    public int nextCheckDelay(long timeWithMills) {
        return 0;
    }
    
    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("start", start);
        tag.putInt("end",end);
        tag.putInt("hourOffset",hourOffset);
    }
    
    @Override
    public ITimeSection load(CompoundTag tag, HolderLookup.Provider registries) {
        var _start = 0;
        var _end = 0;
        var _offset = 0;
        if(tag.contains("start", Tag.TAG_INT)) _start = tag.getInt("start");
        if(tag.contains("end", Tag.TAG_INT)) _end = tag.getInt("end");
        if(tag.contains("hourOffset", Tag.TAG_INT)) _offset = tag.getInt("hourOffset");
        return new DailyCycleTimeSection(_start, _end, _offset);
    }
    
    public static LocalTime fromFormatedInt(int time){
        return LocalTime.of(time /10_000_000, (time %10_000_000)/100_000, (time %100_000)/1000, (time %1000)*1000000);
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
    public int getHourOffset() {
        return hourOffset;
    }
    
}
