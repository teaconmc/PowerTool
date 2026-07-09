package org.teacon.powertool.exhibition.node;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.teacon.powertool.exhibition.HierarchyEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PoseNode extends ExhibitionNode {

    public static final MapCodec<PoseNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ModelPartNode.CODEC.codec().listOf(1, 16).fieldOf("parts").forGetter(PoseNode::getParts)
    ).apply(instance, PoseNode::new));

    public static final StreamCodec<ByteBuf, PoseNode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ModelPartNode.STREAM_CODEC),
            PoseNode::getParts,
            PoseNode::new
    );

    private final List<ModelPartNode> parts;

    public static PoseNode of(final String... parts) {
        var list = new ArrayList<ModelPartNode>(parts.length);
        for (final var part : parts) {
            list.add(new ModelPartNode(part));
        }
        return new PoseNode(list);
    }

    public PoseNode(List<ModelPartNode> parts) {
        this.parts = List.copyOf(parts);
    }

    @Override
    public String name() {
        return "Pose";
    }

    @Override
    public String type() {
        return "pose";
    }

    @Override
    public Collection<HierarchyEntry> children() {
        return List.copyOf(this.parts);
    }

    public List<ModelPartNode> getParts() {
        return this.parts;
    }

}
