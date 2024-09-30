package org.teacon.powertool.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;

public class AutoVanishBoat extends Boat {
    
    protected int idleTickCount = 0;
    
    public AutoVanishBoat(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
    }
    
    public AutoVanishBoat(Level level, double x, double y, double z) {
        super(PowerToolEntities.AUTO_VANISH_BOAT.get(),level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }
    
    @Override
    public void tick() {
        if(!this.level().isClientSide()){
            if(this.getPassengers().isEmpty()){
                idleTickCount++;
            }
            else {
                idleTickCount = 0;
            }
            if(idleTickCount > 401){
                this.discard();
            }
        }
        super.tick();
    }
}
