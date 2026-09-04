package org.teacon.powertool.entity;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.teacon.powertool.item.PowerToolItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AutoVanishBoat extends AbstractBoat {
    
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(AutoVanishBoat.class, EntityDataSerializers.INT);
    private static final List<EntityType<? extends AbstractBoat>> types = List.of(
            EntityType.OAK_BOAT,
            EntityType.SPRUCE_BOAT,
            EntityType.BIRCH_BOAT,
            EntityType.JUNGLE_BOAT,
            EntityType.ACACIA_BOAT,
            EntityType.CHERRY_BOAT,
            EntityType.DARK_OAK_BOAT,
            EntityType.MANGROVE_BOAT,
            EntityType.BAMBOO_RAFT
    );
    protected int idleTickCount = 0;
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
        this.getEntityData().set(TYPE, types.contains(boatType) ? types.indexOf(boatType) : 0);
        if (boatType == EntityType.BAMBOO_RAFT || boatType == EntityType.BAMBOO_CHEST_RAFT)
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
    protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof Player && super.canAddPassenger(passenger);
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
        return types.get(this.getEntityData().get(TYPE));
    }
    
    public Item getDropItem_() {
        var wrapped = getBoatType();
        if (wrapped.equals(EntityType.SPRUCE_BOAT)) return PowerToolItems.AV_SPRUCE_BOAT.get();
        if (wrapped.equals(EntityType.BIRCH_BOAT)) return PowerToolItems.AV_BIRCH_BOAT.get();
        if (wrapped.equals(EntityType.JUNGLE_BOAT)) return PowerToolItems.AV_JUNGLE_BOAT.get();
        if (wrapped.equals(EntityType.ACACIA_BOAT)) return PowerToolItems.AV_ACACIA_BOAT.get();
        if (wrapped.equals(EntityType.CHERRY_BOAT)) return PowerToolItems.AV_CHERRY_BOAT.get();
        if (wrapped.equals(EntityType.DARK_OAK_BOAT)) return PowerToolItems.AV_DARK_OAK_BOAT.get();
        if (wrapped.equals(EntityType.MANGROVE_BOAT)) return PowerToolItems.AV_MANGROVE_BOAT.get();
        if (wrapped.equals(EntityType.BAMBOO_RAFT)) return PowerToolItems.AV_BAMBOO_RAFT.get();
        return PowerToolItems.AV_OAK_BOAT.get();
    }
    
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("type",  this.getEntityData().get(TYPE));
    }
    
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.getEntityData().set(TYPE, input.getIntOr("type",0));
        var boatType = getBoatType();
        if (boatType == EntityType.BAMBOO_RAFT || boatType == EntityType.BAMBOO_CHEST_RAFT)
            this.rideHeightFunc = d -> d.height() * 0.8888889d;
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, 0);
    }
}
