package org.teacon.powertool.client.overlay;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.entity.RegisterBlockEntity;
import org.teacon.powertool.client.ClientEvents;
import org.teacon.powertool.utils.VanillaUtils;

@EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
public class ClientOverlays {
    @SubscribeEvent
    public static void onRegGuiLayerDef(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, VanillaUtils.modRL("cashier_hud"), (guiGraphics, partialTicks) -> {
            Minecraft mc = Minecraft.getInstance();
            HitResult res = mc.hitResult;
            if (mc.level != null && res instanceof BlockHitResult hit) {
                BlockEntity be = mc.level.getBlockEntity(hit.getBlockPos());
                if (be instanceof RegisterBlockEntity theBE && !theBE.itemToAccept.isEmpty()) {
                    var offset = ClientEvents.drawRegisterInfo(mc, guiGraphics, theBE.itemToAccept, 0, 0,
                            Component.translatable("block.powertool.register.hud.prompt.1").withStyle(ChatFormatting.ITALIC),
                            Component.translatable("block.powertool.register.hud.prompt.2", Component.keybind("key.use")).withStyle(ChatFormatting.ITALIC));
                    if (theBE.displaySupply && !theBE.itemToSupply.isEmpty()) {
                        ClientEvents.drawRegisterInfo(mc, guiGraphics, theBE.itemToSupply, offset.x + 8, 0,
                                Component.translatable("block.powertool.register.hud.prompt.3").withStyle(ChatFormatting.ITALIC),
                                Component.empty());
                    }
                }
            }
        });
        event.registerAbove(VanillaGuiLayers.HOTBAR, VanillaUtils.modRL("marting_car_info"), MartingCarOverlay::renderBoostBar);
        if (SharedConstants.IS_RUNNING_WITH_JDWP) {
            event.registerAboveAll(VanillaUtils.modRL("debug_charts"), (guiGraphics, partialTicks) -> {
                for (var value : ClientDebugCharts.DEBUG_CHARTS.values()) {
                    var chart = value.getFirst();
                    chart.extractRenderState(guiGraphics, 0, chart.getWidth(guiGraphics.guiWidth() / 2));
                }
            });
            
        }
    }
}
