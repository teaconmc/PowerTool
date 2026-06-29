package org.teacon.powertool.entity.exhibit;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class ExhibitionHumanoid extends ExhibitionEntity {

    protected static final EntityDataAccessor<ResolvableProfile> DATA_PROFILE;

    private final boolean slim;

    public static @NonNull ExhibitionHumanoid regular(
            final EntityType<ExhibitionHumanoid>    type,
            final Level                             level
    ) {
        return new ExhibitionHumanoid(type, level, false);
    }

    public static @NonNull ExhibitionHumanoid slim(
            final EntityType<ExhibitionHumanoid>    type,
            final Level                             level
    ) {
        return new ExhibitionHumanoid(type, level, true);
    }

    private ExhibitionHumanoid(
            final EntityType<ExhibitionHumanoid>    type,
            final Level                             level,
            final boolean                           slim
    ) {
        super(type, level);
        this.slim       = slim;
    }

    @Override
    protected void defineSynchedData(
            final SynchedEntityData.@NonNull Builder entityData
    ) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_PROFILE, Mannequin.DEFAULT_PROFILE);
    }

    @Override
    public void push(final double xa, final double ya, final double za) {
        return;
    }

    public static AttributeSupplier.@NonNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.ARMOR_TOUGHNESS, 0.0F)
                .add(Attributes.ARMOR, 0.0F)
                .add(Attributes.ATTACK_DAMAGE, 1.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0F)
                .add(Attributes.ATTACK_SPEED, 0.0F)
                .add(Attributes.FOLLOW_RANGE, 32.0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0F)
                .add(Attributes.MAX_HEALTH, 20.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.6F)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0F);
    }

    public boolean isSlim() {
        return this.slim;
    }

    static {
        DATA_PROFILE = SynchedEntityData.defineId(ExhibitionHumanoid.class, EntityDataSerializers.RESOLVABLE_PROFILE);
    }

}
