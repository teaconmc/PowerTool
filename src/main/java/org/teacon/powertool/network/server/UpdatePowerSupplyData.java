package org.teacon.powertool.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.menu.PowerSupplyMenu;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record UpdatePowerSupplyData(int type_, int data) implements CustomPacketPayload {
    
    public static final Type<UpdatePowerSupplyData> TYPE = new Type<>(VanillaUtils.modRL("update_power_supply"));
    
    public static final StreamCodec<ByteBuf,UpdatePowerSupplyData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,UpdatePowerSupplyData::type_,
            ByteBufCodecs.INT,UpdatePowerSupplyData::data,
            UpdatePowerSupplyData::new
    );
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.containerMenu instanceof PowerSupplyMenu theMenu) {
                if (this.type_ == 0) {
                    theMenu.dataHolder.status = this.data;
                    var callback = theMenu.dataHolder.markDirty;
                    if (callback != null) {
                        callback.run();
                    }
                } else if (this.type_ == 1) {
                    theMenu.dataHolder.power = this.data;
                    var callback = theMenu.dataHolder.markDirty;
                    if (callback != null) {
                        callback.run();
                    }
                }
            }
        });
        
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
