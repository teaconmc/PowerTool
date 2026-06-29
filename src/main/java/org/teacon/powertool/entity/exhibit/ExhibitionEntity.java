package org.teacon.powertool.entity.exhibit;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public abstract class ExhibitionEntity extends PathfinderMob {

    protected ExhibitionEntity(
            final EntityType<? extends ExhibitionEntity>    type,
            final Level                                     level
    ) {
        super(type, level);
    }



}
