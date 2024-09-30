package org.teacon.powertool.block.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface IClientUpdateBlockEntity {
    
    void update(CompoundTag tag, HolderLookup.Provider registries);
    
    void writeToPacket(CompoundTag tag, HolderLookup.Provider registries);
}
