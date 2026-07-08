package org.teacon.powertool.network.client;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.client.gui.ExhibitionEntityEditorScreen;
import org.teacon.powertool.entity.exhibit.ExhibitionEntity;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.UUID;

@MethodsReturnNonnullByDefault
public record OpenExhibitionEntityEditor(
        UUID entity
) implements CustomPacketPayload {

    public static final Type<OpenExhibitionEntityEditor> TYPE
            = new Type<>(VanillaUtils.modRL("open_exhibition_entity_editor"));

    public static final StreamCodec<ByteBuf, OpenExhibitionEntityEditor> STREAM_CODEC
            = StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    OpenExhibitionEntityEditor::entity,
                    OpenExhibitionEntityEditor::new
            );

    public static OpenExhibitionEntityEditor of(
            final ExhibitionEntity entity
    ) {
        return new OpenExhibitionEntityEditor(entity.getUUID());
    }

    public void handle(IPayloadContext context) {
        final var uuid  = this.entity;
        context.enqueueWork(() -> {
            var mc      = Minecraft.getInstance();
            var level   = mc.level;

            if (level == null) {
                return;
            }

            var entity  = level.getEntity(uuid);

            if (entity instanceof ExhibitionEntity exhibition) {
                mc.setScreen(ExhibitionEntityEditorScreen.of(exhibition));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
