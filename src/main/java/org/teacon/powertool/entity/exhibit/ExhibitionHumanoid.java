package org.teacon.powertool.entity.exhibit;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.entity.PowerToolEntities;
import org.teacon.powertool.exhibition.ExhibitionNodeManager;
import org.teacon.powertool.exhibition.node.ExhibitionNode;
import org.teacon.powertool.exhibition.node.PoseNode;
import org.teacon.powertool.exhibition.node.SkinNode;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExhibitionHumanoid extends ExhibitionEntity {

    protected static EntityType.EntityFactory<ExhibitionHumanoid> constructor;

    public static @Nullable ExhibitionHumanoid create(
            final EntityType<ExhibitionHumanoid>    type,
            final Level                             level
    ) {
        return constructor.create(type, level);
    }

    public ExhibitionHumanoid(
            final EntityType<ExhibitionHumanoid>    type,
            final Level                             level
    ) {
        super(type, level);
    }

    protected ExhibitionHumanoid(
            final Level                             level
    ) {
        this(PowerToolEntities.EXHIBITION_HUMANOID.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
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

    @Override
    @Contract(pure = true)
    protected void onCreateExhibitionNode(final Consumer<ExhibitionNode> consumer) {
        super.onCreateExhibitionNode(consumer);

        consumer.accept(new SkinNode());
        consumer.accept(PoseNode.of("head", "body", "left_arm", "right_arm", "left_leg", "right_leg"));
    }

    public ResolvableProfile getProfile() {
        final var node = this.getExhibitionNode();
        final var skinNode = node.getUnique(SkinNode.UNIQUE_KEY);
        return skinNode == null ? SkinNode.DEFAULT_PROFILE : skinNode.getProfile();
    }

    static {
        constructor = ExhibitionHumanoid::new;
    }
}
