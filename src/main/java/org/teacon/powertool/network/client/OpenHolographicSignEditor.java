package org.teacon.powertool.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.block.holo_sign.HoloSignBEFlag;
import org.teacon.powertool.block.holo_sign.SignType;
import org.teacon.powertool.client.gui.BaseHolographicSignEditingScreen;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record OpenHolographicSignEditor(BlockPos location, SignType signType) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenHolographicSignEditor> TYPE = new Type<>(VanillaUtils.modResourceLocation("open_holographic_sign_editor"));
    
    public static final StreamCodec<ByteBuf, OpenHolographicSignEditor> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenHolographicSignEditor::location,
            SignType.STREAM_CODEC,
            OpenHolographicSignEditor::signType,
            OpenHolographicSignEditor::new
    );
    
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(new Handler(signType));
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public class Handler implements Runnable {
        private final SignType type;
        
        public Handler(SignType type) {
            this.type = type;
        }
        
        @Override
        public void run() {
            var mc = Minecraft.getInstance();
            var level = mc.level;
            if (level == null) return;
            var te = level.getBlockEntity(OpenHolographicSignEditor.this.location);
            if (te instanceof HoloSignBEFlag) {
                mc.setScreen(BaseHolographicSignEditingScreen.creatHoloSignScreen(te,type));
            }
        }
    }

}
