package org.teacon.powertool.exhibition.node;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.teacon.powertool.inspection.Inspectable;
import org.teacon.powertool.inspection.InspectorBuilder;
import org.teacon.powertool.inspection.property.StringProperty;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SkinNode extends ExhibitionNode implements Inspectable {

    public static final MapCodec<SkinNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(SkinNode::getSkin)
    ).apply(instance, SkinNode::new));

    public static final StreamCodec<ByteBuf, SkinNode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SkinNode::getSkin,
            SkinNode::new
    );

    public static final ResolvableProfile DEFAULT_PROFILE = ResolvableProfile.Static.EMPTY;

    private final StringProperty skin;
    private ResolvableProfile profile = DEFAULT_PROFILE;

    public SkinNode() {
        this("");
    }

    private SkinNode(String skin) {
        this.skin = StringProperty.wrap(
                (String str) -> {
                    if (str == null || str.isBlank()) {
                        this.profile = DEFAULT_PROFILE;
                    } else {
                        this.profile = ResolvableProfile.createUnresolved(str);
                    }
                },
                skin
        );
    }

    @Override
    public void onInspect(InspectorBuilder builder) {
        builder.title(Component.literal(this.name()));
        builder.inputString(
                Component.literal("Skin"),
                this.skin
        );
    }

    @Override
    public String name() {
        return "Skin";
    }

    @Override
    public String type() {
        return "skin";
    }

    public String getSkin() {
        return this.skin.get();
    }

}
