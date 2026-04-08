package org.teacon.powertool.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.teacon.powertool.item.PowerToolItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AutoVanishBoat extends AbstractBoat {
    
    protected int idleTickCount = 0;
    protected EntityType<? extends AbstractBoat> boatType;
    protected EntityType<? extends AbstractBoat> wrapped = EntityType.OAK_BOAT;
    protected Function<EntityDimensions, Double> rideHeightFunc = e -> e.height() / 3d;
    
    public AutoVanishBoat(EntityType<? extends AbstractBoat> entityType, Level level) {
        super(entityType, level, () -> Items.AIR);
        this.dropItem = this::getDropItem_;
    }
    
    public AutoVanishBoat(Level level, double x, double y, double z) {
        super(PowerToolEntities.AUTO_VANISH_BOAT.get(), level, () -> Items.AIR);
        this.dropItem = this::getDropItem_;
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }
    
    public void setWrapped(EntityType<? extends AbstractBoat> boatType) {
        this.wrapped = boatType;
        if (wrapped == EntityType.BAMBOO_RAFT || wrapped == EntityType.BAMBOO_CHEST_RAFT)
            this.rideHeightFunc = d -> d.height() * 0.8888889d;
    }
    
    @SuppressWarnings("unchecked")
    public static AutoVanishBoat fromBoat(AbstractBoat boat) {
        var result = new AutoVanishBoat(boat.level(), boat.xo, boat.yo, boat.zo);
        result.setWrapped((EntityType<? extends AbstractBoat>) boat.getType());
        result.setYRot(boat.getYRot());
        return result;
    }
    
    @Override
    protected double rideHeight(EntityDimensions dimensions) {
        return this.rideHeightFunc.apply(dimensions);
    }
    
    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            if (this.getPassengers().isEmpty()) {
                idleTickCount++;
            } else {
                idleTickCount = 0;
            }
            if (idleTickCount > 401) {
                this.discard();
            }
        }
        super.tick();
    }
    
    public EntityType<?  extends AbstractBoat> getBoatType() {
        return this.boatType;
    }
    
    public Item getDropItem_() {
        if (boatType.equals(EntityType.SPRUCE_BOAT)) return PowerToolItems.AV_SPRUCE_BOAT.get();
        if (boatType.equals(EntityType.BIRCH_BOAT)) return PowerToolItems.AV_BIRCH_BOAT.get();
        if (boatType.equals(EntityType.JUNGLE_BOAT)) return PowerToolItems.AV_JUNGLE_BOAT.get();
        if (boatType.equals(EntityType.ACACIA_BOAT)) return PowerToolItems.AV_ACACIA_BOAT.get();
        if (boatType.equals(EntityType.CHERRY_BOAT)) return PowerToolItems.AV_CHERRY_BOAT.get();
        if (boatType.equals(EntityType.DARK_OAK_BOAT)) return PowerToolItems.AV_DARK_OAK_BOAT.get();
        if (boatType.equals(EntityType.MANGROVE_BOAT)) return PowerToolItems.AV_MANGROVE_BOAT.get();
        if (boatType.equals(EntityType.BAMBOO_RAFT)) return PowerToolItems.AV_BAMBOO_RAFT.get();
        return PowerToolItems.AV_OAK_BOAT.get();
    }
}
