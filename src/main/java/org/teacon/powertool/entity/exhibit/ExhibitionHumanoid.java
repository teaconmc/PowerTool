package org.teacon.powertool.entity.exhibit;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.exhibition.node.ExhibitionNode;
import org.teacon.powertool.exhibition.node.PoseNode;
import org.teacon.powertool.exhibition.node.SkinNode;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExhibitionHumanoid extends ExhibitionEntity {

    private final boolean slim;

    public static ExhibitionHumanoid regular(
            final EntityType<ExhibitionHumanoid>    type,
            final Level                             level
    ) {
        return new ExhibitionHumanoid(type, level, false);
    }

    public static ExhibitionHumanoid slim(
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
    protected void onCreateExhibitionNode(final Consumer<ExhibitionNode> consumer) {
        super.onCreateExhibitionNode(consumer);

        consumer.accept(new SkinNode());
        consumer.accept(PoseNode.of("head", "body", "left_arm", "right_arm", "left_leg", "right_leg"));
    }

    public boolean isSlim() {
        return this.slim;
    }
}
