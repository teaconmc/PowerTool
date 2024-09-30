package org.teacon.powertool.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.block.entity.TimeObserverBlockEntity;
import org.teacon.powertool.client.gui.observers.GameTimeCycleObserverScreen;
import org.teacon.powertool.client.gui.observers.RealTimeCycleObserverScreen;
import org.teacon.powertool.client.gui.observers.RealTimeObserverScreen;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record OpenBlockScreen(BlockPos pos, int guiType) implements CustomPacketPayload{
    
    public static final int REAL_TIME_OBSERVER = 1;
    public static final int REAL_TIME_CYCLE_OBSERVER = 2;
    public static final int GAME_TIME_CYCLE_OBSERVER = 3;
    
    public static final CustomPacketPayload.Type<OpenBlockScreen> TYPE = new Type<>(VanillaUtils.modRL("open_gui"));
    
    public static final StreamCodec<ByteBuf, OpenBlockScreen> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenBlockScreen::pos,
            ByteBufCodecs.VAR_INT,
            OpenBlockScreen::guiType,
            OpenBlockScreen::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public void handle(IPayloadContext context){
        context.enqueueWork(() -> Handler.run(this));
    }
    
    public static class Handler{
        public static void run(OpenBlockScreen pack){
            var level = Minecraft.getInstance().level;
            if(level == null) return;
            var te = level.getBlockEntity(pack.pos);
            if(pack.guiType == REAL_TIME_OBSERVER && te instanceof TimeObserverBlockEntity _te){
                Minecraft.getInstance().setScreen(new RealTimeObserverScreen(_te));
            }
            else if(pack.guiType == REAL_TIME_CYCLE_OBSERVER && te instanceof TimeObserverBlockEntity _te){
                Minecraft.getInstance().setScreen(new RealTimeCycleObserverScreen(_te));
            }
            else if(pack.guiType == GAME_TIME_CYCLE_OBSERVER && te instanceof TimeObserverBlockEntity _te){
                Minecraft.getInstance().setScreen(new GameTimeCycleObserverScreen(_te));
            }
        }
    }
}
