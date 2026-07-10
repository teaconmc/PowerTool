package org.teacon.powertool.exhibition.node;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.teacon.powertool.exhibition.HierarchyEntry;
import org.teacon.powertool.inspection.Duplicatable;
import org.teacon.powertool.inspection.Inspectable;
import org.teacon.powertool.inspection.InspectorBuilder;
import org.teacon.powertool.inspection.constraint.NumberConstraint;
import org.teacon.powertool.inspection.property.BooleanProperty;
import org.teacon.powertool.inspection.property.Vector3fProperty;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModelPartNode extends ExhibitionNode implements Inspectable {

    public static final MapCodec<ModelPartNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("partName").forGetter(ModelPartNode::getPartName),
            ExtraCodecs.VECTOR3F.fieldOf("position").forGetter(ModelPartNode::getPosition),
            ExtraCodecs.VECTOR3F.fieldOf("rotation").forGetter(ModelPartNode::getRotation),
            ExtraCodecs.VECTOR3F.fieldOf("scale").forGetter(ModelPartNode::getScale),
            Codec.BOOL.fieldOf("visible").forGetter(ModelPartNode::isVisible)
    ).apply(instance, ModelPartNode::new));

    public static final StreamCodec<ByteBuf, ModelPartNode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ModelPartNode::getPartName,
            ByteBufCodecs.VECTOR3F,
            ModelPartNode::getPosition,
            ByteBufCodecs.VECTOR3F,
            ModelPartNode::getRotation,
            ByteBufCodecs.VECTOR3F,
            ModelPartNode::getScale,
            ByteBufCodecs.BOOL,
            ModelPartNode::isVisible,
            ModelPartNode::new
    );

    public static final NumberConstraint<Float> POSITION = NumberConstraint.number(
            -128f,
            128f,
            0.0f
    );

    public static final NumberConstraint<Float> SCALE = NumberConstraint.number(
            0.0f,
            128f,
            1.0f
    );

    private final String partName;
    private final Vector3fProperty position;
    private final Vector3fProperty rotation;
    private final Vector3fProperty scale;
    private final BooleanProperty  visible;

    public static ModelPartNode create(final String name) {
        return new ModelPartNode(
                name,
                new Vector3f(),
                new Vector3f(),
                new Vector3f(1.0F),
                true
        );
    }

    public ModelPartNode(
            final String    partName,
            final Vector3fc position,
            final Vector3fc rotation,
            final Vector3fc scale,
            final boolean visible
    ) {
        this.partName   = partName;
        this.position   = Vector3fProperty.of(position);
        this.rotation   = Vector3fProperty.of(rotation);
        this.scale      = Vector3fProperty.of(scale);
        this.visible    = BooleanProperty.simple(visible);
    }

    public ModelPartNode(final String partName) {
        this(partName, new Vector3f(), new Vector3f(), new Vector3f(1.0F), true);
    }

    public String getPartName() {
        return this.partName;
    }

    public Vector3fc getPosition() {
        return this.position.get();
    }

    public Vector3fc getRotation() {
        return this.rotation.get();
    }

    public Vector3fc getScale() {
        return this.scale.get();
    }

    public boolean isVisible() {
        return this.visible.get();
    }

    @Override
    public void onInspect(final InspectorBuilder builder) {
        builder     .title(Component.literal(this.partName))
                    .title(Component.literal("Position"))
                    .inputFloat(Component.literal("X"), this.position.x, POSITION)
                    .inputFloat(Component.literal("Y"), this.position.y, POSITION)
                    .inputFloat(Component.literal("Z"), this.position.z, POSITION)
                    .space()
                    .title(Component.literal("Rotation"))
                    .inputFloat(Component.literal("X"), this.rotation.x, NumberConstraint.DEGREES)
                    .inputFloat(Component.literal("Y"), this.rotation.y, NumberConstraint.DEGREES)
                    .inputFloat(Component.literal("Z"), this.rotation.z, NumberConstraint.DEGREES)
                    .space()
                    .title(Component.literal("Scale"))
                    .inputFloat(Component.literal("X"), this.scale.x, SCALE)
                    .inputFloat(Component.literal("Y"), this.scale.y, SCALE)
                    .inputFloat(Component.literal("Z"), this.scale.z, SCALE)
                    .space()
                    .checkbox(Component.literal("Visible"), this.visible);

    }

    @Override
    public String name() {
        return this.partName;
    }

    @Override
    public String type() {
        return "part";
    }

    @Override
    public ModelPartNode duplicate() {
        return new ModelPartNode(
                this.partName,
                this.position.get(),
                this.rotation.get(),
                this.scale.get(),
                this.visible.get()
        );
    }

    @Override
    public void paste(final Duplicatable other) {
        if (other.getClass() != ModelPartNode.class) {
            return;
        }

        final ModelPartNode node = (ModelPartNode) other;

        /*if (!this.partName.equals(node.partName)) {
            return;
        }*/

        this.position.set(node.position.get());
        this.rotation.set(node.rotation.get());
        this.scale.set(node.scale.get());
        this.visible.set(node.visible.get());
    }

}
