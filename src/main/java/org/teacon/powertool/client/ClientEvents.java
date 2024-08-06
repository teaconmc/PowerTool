package org.teacon.powertool.client;

import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.client.renders.FenceKnotRenderer;
import org.teacon.powertool.client.renders.HolographicSignBlockEntityRenderer;
import org.teacon.powertool.client.renders.ItemDisplayBlockEntityRenderer;
import org.teacon.powertool.client.renders.ItemSupplierBlockEntityRenderer;
import org.teacon.powertool.client.gui.PeriodicCommandBlockEditScreen;
import org.teacon.powertool.client.gui.PowerSupplyScreen;
import org.teacon.powertool.client.renders.LinkHolographicSignBlockEntityRenderer;
import org.teacon.powertool.entity.PowerToolEntities;
import org.teacon.powertool.menu.PowerToolMenus;

@EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
public class ClientEvents {

    public static int tickCount = 0;

    @SubscribeEvent
    public static void on(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof CommandBlockEditScreen screen
            && screen.autoCommandBlock instanceof PeriodicCommandBlockEntity blockEntity) {
            event.setNewScreen(new PeriodicCommandBlockEditScreen(blockEntity));
        }
    }

    @SubscribeEvent
    public static void on(ClientTickEvent.Pre event) {
        tickCount++;
    }

    @EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = PowerTool.MODID)
    public static final class OnModBus {
        @SubscribeEvent
        public static void setup(final RegisterMenuScreensEvent event) {
            event.register(PowerToolMenus.POWER_SUPPLY_MENU.get(), PowerSupplyScreen::new);
        }
        @SubscribeEvent
        public static void ber(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(PowerToolEntities.FENCE_KNOT.get(), FenceKnotRenderer::new);

            event.registerBlockEntityRenderer(PowerToolBlocks.ITEM_DISPLAY_BLOCK_ENTITY.get(), ItemDisplayBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.ITEM_SUPPLIER_BLOCK_ENTITY.get(), ItemSupplierBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), HolographicSignBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.LINK_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), LinkHolographicSignBlockEntityRenderer::new);
        }
    }
}
