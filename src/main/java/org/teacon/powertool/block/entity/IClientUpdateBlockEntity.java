package org.teacon.powertool.block.entity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface IClientUpdateBlockEntity {
    
    void updateFromClient(ValueInput input);
    
    void writeFromClient(ValueOutput output);
}
