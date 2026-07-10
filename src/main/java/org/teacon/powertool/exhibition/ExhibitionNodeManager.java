package org.teacon.powertool.exhibition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKey;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.entity.exhibit.ExhibitionEntity;
import org.teacon.powertool.exhibition.node.ExhibitionNode;
import org.teacon.powertool.inspection.Duplicatable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ExhibitionNodeManager
        implements HierarchyEntry {

    public static final Codec<ExhibitionNodeManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExhibitionNode.CODEC.listOf().fieldOf("nodes").forGetter(ExhibitionNodeManager::nodes)
    ).apply(instance, ExhibitionNodeManager::new));

    public static final StreamCodec<ByteBuf, ExhibitionNodeManager> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ExhibitionNode.STREAM_CODEC),
            ExhibitionNodeManager::nodes,
            ExhibitionNodeManager::new
    );

    private final List<ExhibitionNode> nodes;
    private final Map<ContextKey<? extends ExhibitionNode>, ExhibitionNode> unique;

    private boolean dirty;
    private boolean editing;
    private int id;

    public ExhibitionNodeManager(final List<ExhibitionNode> nodes) {
        this.nodes  = nodes;
        this.unique = collectUnique(nodes);
    }

    public void setup(final ExhibitionEntity entity) {

        this.id = entity.getId();
        ExhibitionNode.walk(this.nodes, node -> node.init(entity));

    }

    public void apply(final ExhibitionEntity entity) {

        this.id = entity.getId();
        ExhibitionNode.walk(this.nodes, node -> node.apply(entity));

    }

    @SuppressWarnings("unchecked")
    public <T extends ExhibitionNode> T getUnique(final ContextKey<T> key) {
        if (this.unique.containsKey(key)) {
            return (T) this.unique.get(key);
        }
        return null;
    }

    @Override
    public @NonNull ExhibitionNodeManager duplicate() {
        final var nodes = new ArrayList<ExhibitionNode>();
        for (final var node : this.nodes) {
            nodes.add(node.duplicate());
        }
        return new ExhibitionNodeManager(nodes);
    }

    @Override
    public void paste(final Duplicatable other) {

        List<ExhibitionNode> nodes;
        if (other.getClass() == ExhibitionNodeManager.class) {
            nodes = ((ExhibitionNodeManager) other).nodes;
        } else if (other.getClass() == ExhibitionNodeManager.Immutable.class) {
            nodes = ((ExhibitionNodeManager.Immutable) other).nodes;
        } else {
            return;
        }

        if (this.nodes.size() != nodes.size()) {
            return;
        }

        // suppose both manager has same structure of nodes
        for (int i = 0; i < this.nodes.size(); i++) {
            this.nodes.get(i).paste(nodes.get(i));
        }
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

    private static Map<ContextKey<? extends ExhibitionNode>, ExhibitionNode> collectUnique(final List<ExhibitionNode> nodes) {

        final var builder   = ImmutableMap.<ContextKey<? extends ExhibitionNode>, ExhibitionNode>builder();

        final Consumer<ExhibitionNode> collector = (node) -> {
            final var key = node.uniqueKey();
            if (key != null) {
                builder.put(key, node);
            }
        };

        ExhibitionNode.walk(nodes, collector);

        return builder.build();

    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(final boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isEditing() {
        return this.editing;
    }

    public void setEditing(final boolean editing) {
        this.editing = editing;
    }

    public Immutable toImmutable() {
        return Immutable.of(this);
    }

    public record Immutable(
            List<ExhibitionNode> nodes
    ) implements Duplicatable {

        public static Immutable of(final ExhibitionNodeManager manager) {
            final var builder = ImmutableList.<ExhibitionNode>builder();
            for (final var node : manager.nodes) {
                builder.add(node.duplicate());
            }
            return new Immutable(builder.build());
        }

        public static final Codec<ExhibitionNodeManager.Immutable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExhibitionNode.CODEC.listOf().fieldOf("nodes").forGetter(ExhibitionNodeManager.Immutable::nodes)
        ).apply(instance, ExhibitionNodeManager.Immutable::new));

        public static final StreamCodec<ByteBuf, ExhibitionNodeManager.Immutable> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(ArrayList::new, ExhibitionNode.STREAM_CODEC),
                ExhibitionNodeManager.Immutable::nodes,
                ExhibitionNodeManager.Immutable::new
        );

        public ExhibitionNodeManager toMutable() {
            return new ExhibitionNodeManager(this.nodes);
        }

        @Override
        public @NonNull Duplicatable duplicate() {
            final var builder = ImmutableList.<ExhibitionNode>builder();
            for (final var node : this.nodes) {
                builder.add(node.duplicate());
            }
            return new Immutable(builder.build());
        }
    }

}
