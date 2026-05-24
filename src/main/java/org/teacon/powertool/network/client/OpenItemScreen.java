package org.teacon.powertool.network.client;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.teacon.powertool.client.PowerToolScreenProviders;
import org.teacon.powertool.utils.VanillaUtils;

@MethodsReturnNonnullByDefault
public record OpenItemScreen(ItemStack stack, EquipmentSlot slot) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<OpenItemScreen> TYPE = new Type<>(VanillaUtils.modRL("open_item_screen"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenItemScreen> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            OpenItemScreen::stack,
            ByteBufCodecs.fromCodec(EquipmentSlot.CODEC),
            OpenItemScreen::slot,
            OpenItemScreen::new
    );
    
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            var screenProvider = PowerToolScreenProviders.SCREEN_PROVIDERS.get(id);
            if (screenProvider != null) {
                mc.setScreen(screenProvider.createScreen(stack, slot));
            }
        });
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
