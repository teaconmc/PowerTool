package org.teacon.powertool.exhibition.node;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.inspection.Inspectable;
import org.teacon.powertool.inspection.InspectorBuilder;
import org.teacon.powertool.inspection.constraint.NumberConstraint;
import org.teacon.powertool.inspection.property.FloatProperty;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EntityNode extends ExhibitionNode implements Inspectable {

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

    private static final NumberConstraint<Float> POSITION = NumberConstraint.number(
            Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            0
    );

    private static final NumberConstraint<Float> DEGREES = NumberConstraint.number(
            -180f,
            180f,
            0
    );

    private final FloatProperty x;
    private final FloatProperty y;
    private final FloatProperty z;

    private final FloatProperty yaw;
    private final FloatProperty pitch;

    public static EntityNode of(@NonNull Entity entity) {
        return new EntityNode(
                (float) entity.getX(),
                (float) entity.getY(),
                (float) entity.getZ(),
                entity.getYRot(),
                entity.getXRot()
        );
    }

    public EntityNode(
            float x,
            float y,
            float z,
            float yaw,
            float pitch
    ) {
        this.x      = FloatProperty.simple(x);
        this.y      = FloatProperty.simple(y);
        this.z      = FloatProperty.simple(z);
        this.yaw    = FloatProperty.simple(yaw);
        this.pitch  = FloatProperty.simple(pitch);
    }

    @Override
    public void onInspect(InspectorBuilder builder) {
        builder .title(Component.literal(this.name()))

                .title(Component.literal("Position"))
                .inputFloat(Component.literal("X"), this.x, POSITION)
                .inputFloat(Component.literal("Y"), this.y, POSITION)
                .inputFloat(Component.literal("Z"), this.z, POSITION)

                .separator()
                .title(Component.literal("Rotation"))
                .inputFloat(Component.literal("Yaw"), this.yaw, DEGREES)
                .inputFloat(Component.literal("Pitch"), this.pitch, DEGREES);
    }

    @Override
    public String name() {
        return "Entity";
    }

    @Override
    public String type() {
        return "entity";
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
