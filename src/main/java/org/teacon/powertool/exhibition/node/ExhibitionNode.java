package org.teacon.powertool.exhibition.node;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.teacon.powertool.PowerTool;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ExhibitionNode {

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

    public abstract String name();

    public abstract String type();

    public Collection<ExhibitionNode> children() {
        return Collections.emptyList();
    }

    private static void setup() {

        var builder = ImmutableMap.<String, Serializer>builder();

        builder.put("root", Serializer.of(RootNode.CODEC, RootNode.STREAM_CODEC));
        builder.put("entity", Serializer.of(EntityNode.CODEC, EntityNode.STREAM_CODEC));
        builder.put("skin", Serializer.of(SkinNode.CODEC, SkinNode.STREAM_CODEC));
        builder.put("pose", Serializer.of(PoseNode.CODEC, PoseNode.STREAM_CODEC));

        SERIALIZERS = builder.build();

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
