package org.teacon.powertool.exhibition.node;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.entity.exhibit.ExhibitionEntity;
import org.teacon.powertool.inspection.Inspectable;
import org.teacon.powertool.inspection.InspectorBuilder;
import org.teacon.powertool.inspection.constraint.NumberConstraint;
import org.teacon.powertool.inspection.property.FloatProperty;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EntityNode extends ExhibitionNode implements Inspectable {

    public static final ContextKey<EntityNode> UNIQUE_KEY = EntityNode.createUniqueKey("entity");

    public static final MapCodec<EntityNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("x").forGetter(EntityNode::getX),
            Codec.FLOAT.fieldOf("y").forGetter(EntityNode::getY),
            Codec.FLOAT.fieldOf("z").forGetter(EntityNode::getZ),
            Codec.FLOAT.fieldOf("yaw").forGetter(EntityNode::getYaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(EntityNode::getPitch)
    ).apply(instance, EntityNode::new));

    public static final StreamCodec<ByteBuf, EntityNode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            EntityNode::getX,
            ByteBufCodecs.FLOAT,
            EntityNode::getY,
            ByteBufCodecs.FLOAT,
            EntityNode::getZ,
            ByteBufCodecs.FLOAT,
            EntityNode::getYaw,
            ByteBufCodecs.FLOAT,
            EntityNode::getPitch,
            EntityNode::new
    );

    private final FloatProperty x       = FloatProperty.simple(0);
    private final FloatProperty y       = FloatProperty.simple(0);
    private final FloatProperty z       = FloatProperty.simple(0);

    private final FloatProperty yaw     = FloatProperty.simple(0);
    private final FloatProperty pitch   = FloatProperty.simple(0);

    public static EntityNode of(Entity entity) {
        return new EntityNode(
                (float) entity.getX(),
                (float) entity.getY(),
                (float) entity.getZ(),
                entity.getYRot() * Mth.RAD_TO_DEG,
                entity.getXRot() * Mth.RAD_TO_DEG
        );
    }

    public EntityNode(
            float x,
            float y,
            float z,
            float yaw,
            float pitch
    ) {
        this.setup(x, y, z, yaw, pitch);
    }

    @Override
    public void onInspect(InspectorBuilder builder) {
        builder .title(Component.literal(this.name()))

                .title(Component.literal("Position"))
                .inputFloat(Component.literal("X"), this.x)
                .inputFloat(Component.literal("Y"), this.y)
                .inputFloat(Component.literal("Z"), this.z)

                .separator()
                .title(Component.literal("Rotation"))
                .inputFloat(Component.literal("Yaw"), this.yaw, NumberConstraint.DEGREES)
                .inputFloat(Component.literal("Pitch"), this.pitch, NumberConstraint.DEGREES);
    }

    @Override
    public String name() {
        return "Entity";
    }

    @Override
    public String type() {
        return "entity";
    }

    @Override
    public ExhibitionNode duplicate() {
        return new EntityNode(
                this.x.getValue(),
                this.y.getValue(),
                this.z.getValue(),
                this.yaw.getValue(),
                this.pitch.getValue()
        );
    }

    @Override
    public @Nullable ContextKey<? extends ExhibitionNode> uniqueKey() {
        return UNIQUE_KEY;
    }

    @Override
    public void init(final ExhibitionEntity entity) {
        this.setup(
                (float) entity.getX(),
                (float) entity.getY(),
                (float) entity.getZ(),
                entity.getYRot(),
                entity.getXRot()
        );
    }

    @Override
    public void apply(final ExhibitionEntity entity) {
        entity.setPos(
                this.x.getNumber(),
                this.y.getNumber(),
                this.z.getNumber()
        );

        final var yaw   = this.yaw.getValue() * Mth.DEG_TO_RAD;
        final var pitch = this.pitch.getValue() * Mth.DEG_TO_RAD;
        entity.setYRot(yaw);
        entity.setYBodyRot(yaw);
        entity.setXRot(pitch);
    }

    private void setup(
            float x,
            float y,
            float z,
            float yaw,
            float pitch
    ) {
        this.x      .setValue(x);
        this.y      .setValue(y);
        this.z      .setValue(z);
        this.yaw    .setValue(yaw * Mth.RAD_TO_DEG);
        this.pitch  .setValue(pitch * Mth.RAD_TO_DEG);
    }

    public float getX() {
        return this.x.getValue();
    }

    public float getY() {
        return this.y.getValue();
    }

    public float getZ() {
        return this.z.getValue();
    }

    public float getYaw() {
        return this.yaw.getValue();
    }

    public float getPitch() {
        return this.pitch.getValue();
    }
}
