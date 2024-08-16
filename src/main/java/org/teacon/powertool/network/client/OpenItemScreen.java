package org.teacon.powertool.network.client;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.item.IScreenProviderItem;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record OpenItemScreen(ItemStack stack, EquipmentSlot slot) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<OpenItemScreen> TYPE = new Type<>(VanillaUtils.modResourceLocation("open_item_screen"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenItemScreen> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            OpenItemScreen::stack,
            ByteBufCodecs.fromCodec(EquipmentSlot.CODEC),
            OpenItemScreen::slot,
            OpenItemScreen::new
    );
    
    public void handle(IPayloadContext context){
        context.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if(stack.getItem() instanceof IScreenProviderItem screenProvider){
                mc.setScreen(screenProvider.getScreenSupplier(stack,slot).get());
            }
        });
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
