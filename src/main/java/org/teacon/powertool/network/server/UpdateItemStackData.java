package org.teacon.powertool.network.server;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.Map;
import java.util.Optional;

@MethodsReturnNonnullByDefault
public record UpdateItemStackData(EquipmentSlot slot, DataComponentPatch componentPatch) implements CustomPacketPayload {
    
    public static final Type<UpdateItemStackData> TYPE = new Type<>(VanillaUtils.modResourceLocation("update_item_stack_data"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateItemStackData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(EquipmentSlot.CODEC),
            UpdateItemStackData::slot,
            DataComponentPatch.STREAM_CODEC,
            UpdateItemStackData::componentPatch,
            UpdateItemStackData::new
    );
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void handle(IPayloadContext context){
        context.enqueueWork(() -> {
            var player = context.player();
            var itemOnPlayer = player.getItemBySlot(slot);
            for(Map.Entry<DataComponentType<?>, Optional<?>> data: componentPatch.entrySet()){
                if(data.getValue().isEmpty()) continue;
                Object value = data.getValue().get();
                itemOnPlayer.set((DataComponentType) data.getKey(),value);
            }
        });
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
