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
import org.teacon.powertool.inspection.Inspectable;
import org.teacon.powertool.inspection.InspectorBuilder;
import org.teacon.powertool.inspection.property.Vector3fProperty;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModelPartNode extends ExhibitionNode implements Inspectable {

    public static final MapCodec<ModelPartNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("partName").forGetter(ModelPartNode::getPartName),
            ExtraCodecs.VECTOR3F.fieldOf("position").forGetter(ModelPartNode::getPosition),
            ExtraCodecs.VECTOR3F.fieldOf("rotation").forGetter(ModelPartNode::getRotation),
            ExtraCodecs.VECTOR3F.fieldOf("scale").forGetter(ModelPartNode::getScale)
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
            ModelPartNode::new
    );

    private final String partName;
    private final Vector3fProperty position;
    private final Vector3fProperty rotation;
    private final Vector3fProperty scale;

    public static ModelPartNode create(final String name) {
        return new ModelPartNode(
                name,
                new Vector3f(),
                new Vector3f(),
                new Vector3f(1.0F)
        );
    }

    public ModelPartNode(
            final String    partName,
            final Vector3fc position,
            final Vector3fc rotation,
            final Vector3fc scale
    ) {
        this.partName   = partName;
        this.position   = Vector3fProperty.of(position);
        this.rotation   = Vector3fProperty.of(rotation);
        this.scale      = Vector3fProperty.of(scale);
    }

    public ModelPartNode(final String partName) {
        this(partName, new Vector3f(), new Vector3f(), new Vector3f(1.0F));
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

    @Override
    public void onInspect(final InspectorBuilder builder) {
        builder.title(Component.literal(this.partName));
    }

    @Override
    public String name() {
        return this.partName;
    }

    @Override
    public String type() {
        return "part";
    }

}
