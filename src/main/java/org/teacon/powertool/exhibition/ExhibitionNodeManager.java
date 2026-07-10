package org.teacon.powertool.exhibition;

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

        if (!(other.getClass() == ExhibitionNodeManager.class)) {
            return;
        }

        final var manager = (ExhibitionNodeManager) other;

        if (this.nodes.size() != manager.nodes.size()) {
            return;
        }

        // suppose both manager has same structure of nodes
        for (int i = 0; i < this.nodes.size(); i++) {
            this.nodes.get(i).paste(manager.nodes.get(i));
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
}
