package org.teacon.powertool.exhibition.node;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.entity.exhibit.ExhibitionEntity;
import org.teacon.powertool.exhibition.HierarchyEntry;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ExhibitionNode
        implements HierarchyEntry {

    private static Map<String, Serializer> SERIALIZERS = new HashMap<>();

    // 暂时不用 registry, 麻烦
    public static final Codec<ExhibitionNode> CODEC = Codec.STRING.dispatch(
            ExhibitionNode::type,
            type -> SERIALIZERS.get(type).codec()
    );

    public static final StreamCodec<ByteBuf, ExhibitionNode> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.dispatch(
            ExhibitionNode::type,
            type -> SERIALIZERS.get(type).streamCodec()
    );

    public abstract String type();

    public abstract ExhibitionNode duplicate();

    public @Nullable ContextKey<? extends ExhibitionNode> uniqueKey() {
        return null;
    }

    public void init(final ExhibitionEntity entity) { }

    public void apply(final ExhibitionEntity entity) { }

    @Override
    public Collection<HierarchyEntry> children() {
        return Collections.emptyList();
    }

    public static void walk(
            ExhibitionNode root,
            Consumer<ExhibitionNode> consumer
    ) {
        final Stack<ExhibitionNode> stack = new ObjectArrayList<>();
        stack.push(root);

        _walk(stack, consumer);
    }

    public static void walk(
            Collection<ExhibitionNode> nodes,
            Consumer<ExhibitionNode> consumer
    ) {
        final Stack<ExhibitionNode> stack = new ObjectArrayList<>();
        nodes.forEach(stack::push);

        _walk(stack, consumer);
    }

    private static void _walk(
            Stack<ExhibitionNode> stack,
            Consumer<ExhibitionNode> consumer
    ) {
        while (!stack.isEmpty()) {
            var node = stack.pop();
            consumer.accept(node);

            for (var child : node.children()) {
                if (child instanceof ExhibitionNode exhibition) {
                    stack.push(exhibition);
                }
            }
        }
    }

    private static void setup() {

        var builder = ImmutableMap.<String, Serializer>builder();

        builder.put("entity", Serializer.of(EntityNode.CODEC, EntityNode.STREAM_CODEC));
        builder.put("skin", Serializer.of(SkinNode.CODEC, SkinNode.STREAM_CODEC));
        builder.put("pose", Serializer.of(PoseNode.CODEC, PoseNode.STREAM_CODEC));
        builder.put("interact", Serializer.of(InteractNode.CODEC, InteractNode.STREAM_CODEC));
        builder.put("command", Serializer.of(CommandNode.CODEC, CommandNode.STREAM_CODEC));

        SERIALIZERS = builder.build();

    }

    protected static <T extends ExhibitionNode> ContextKey<T> createUniqueKey(String name) {
        return new ContextKey<>(VanillaUtils.modRL(name));
    }

    @EventBusSubscriber(modid = PowerTool.MODID)
    private static final class Handler {

        @SubscribeEvent
        public static void onRegisterEvent(final RegisterEvent event) {

            ExhibitionNode.setup();

        }

    }

    public static final class Serializer {
        private final MapCodec<? extends ExhibitionNode> codec;
        private final StreamCodec<ByteBuf, ? extends ExhibitionNode> streamCodec;

        private Serializer(
                MapCodec<? extends ExhibitionNode> codec,
                StreamCodec<ByteBuf, ? extends ExhibitionNode> streamCodec
        ) {
            this.codec = codec;
            this.streamCodec = streamCodec;
        }

        public static <T extends ExhibitionNode> Serializer of(
                    final MapCodec<T> codec,
                    final StreamCodec<ByteBuf, T> streamCodec
            ) {
                return new Serializer(codec, streamCodec);
            }

        public MapCodec<? extends ExhibitionNode> codec() {
            return codec;
        }

        public StreamCodec<ByteBuf, ? extends ExhibitionNode> streamCodec() {
            return streamCodec;
        }
    }

}
