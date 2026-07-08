package org.teacon.powertool.exhibition.node;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class RootNode extends ExhibitionNode {

    public static final MapCodec<RootNode> CODEC
            = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ExhibitionNode.CODEC.listOf().fieldOf("nodes").forGetter(RootNode::children)
            ).apply(instance, RootNode::new));

    public static final StreamCodec<ByteBuf, RootNode> STREAM_CODEC
            = StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, ExhibitionNode.STREAM_CODEC),
                    RootNode::children,
                    RootNode::new
            );

    private final List<ExhibitionNode> nodes;

    public RootNode(final List<ExhibitionNode> nodes) {
        // check do not have RootNode in nodes
        for (var n : nodes) {
            if (n.getClass() == RootNode.class) {
                throw new IllegalArgumentException("RootNode cannot be in nodes");
            }
        }
        this.nodes = nodes;
    }

    @Override
    public @NonNull String name() {
        return "Root";
    }

    @Override
    public @NonNull String type() {
        return "root";
    }

    @Override
    public @NonNull List<ExhibitionNode> children() {
        return this.nodes;
    }
}
