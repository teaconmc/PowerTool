package org.teacon.powertool.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.block.entity.HolographicSignBlockEntity;
import org.teacon.powertool.client.HolographicSignEditingScreen;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record OpenHolographicSignEditor(BlockPos location) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenHolographicSignEditor> TYPE = new Type<>(VanillaUtils.modResourceLocation("open_holographic_sign_editor"));
    
    public static final StreamCodec<ByteBuf,OpenHolographicSignEditor> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,OpenHolographicSignEditor::location,
            OpenHolographicSignEditor::new
    );
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(new Handler());
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public class Handler implements Runnable {
        @Override
        public void run() {
            var mc = Minecraft.getInstance();
            var level = mc.level;
            if (level != null && level.getBlockEntity(OpenHolographicSignEditor.this.location) instanceof HolographicSignBlockEntity theSign) {
                mc.setScreen(new HolographicSignEditingScreen(theSign));
            }
        }
    }

}
