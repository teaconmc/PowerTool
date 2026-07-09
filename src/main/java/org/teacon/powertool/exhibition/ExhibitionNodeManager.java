package org.teacon.powertool.exhibition;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.teacon.powertool.exhibition.node.ExhibitionNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ExhibitionNodeManager implements HierarchyEntry {

    public static final Codec<ExhibitionNodeManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExhibitionNode.CODEC.listOf().fieldOf("nodes").forGetter(ExhibitionNodeManager::nodes)
    ).apply(instance, ExhibitionNodeManager::new));

    public static final StreamCodec<ByteBuf, ExhibitionNodeManager> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ExhibitionNode.STREAM_CODEC),
            ExhibitionNodeManager::nodes,
            ExhibitionNodeManager::new
    );

    private final List<ExhibitionNode> nodes;
    private final Map<String, ExhibitionNode> unique;

    public ExhibitionNodeManager(final List<ExhibitionNode> nodes) {
        this.nodes  = nodes;
        this.unique = collectUnique(nodes);
    }

    public ExhibitionNode get(final String name) {
        return this.unique.get(name);
    }

    @Override
    public String name() {
        return "root";
    }

    @Override
    public Collection<HierarchyEntry> children() {
        return List.copyOf(this.nodes);
    }

    private List<ExhibitionNode> nodes() {
        return this.nodes;
    }

    private static Map<String, ExhibitionNode> collectUnique(final List<ExhibitionNode> nodes) {

        final var counts    = new Object2IntOpenHashMap<String>();
        final var mapping   = new Object2ObjectOpenHashMap<String, ExhibitionNode>();

        final Consumer<ExhibitionNode> collector = (node) -> {
            final var name = node.name();
            // if already exist, then count + 1, but do not add it to the map
            final var count = counts.getOrDefault(name, 0);
            if (count == 0) {
                mapping.put(name, node);
            }
            counts.mergeInt(name, 1, Integer::sum);
        };

        for (var node : nodes) {
            ExhibitionNode.walk(node, collector);
        }

        final var builder   = ImmutableMap.<String, ExhibitionNode>builder();
        for (var entry : mapping.entrySet()) {
            final var name  = entry.getKey();

            if (counts.getInt(name) == 1) {
                builder.put(name, entry.getValue());
            }
        }

        return builder.build();

    }
}
