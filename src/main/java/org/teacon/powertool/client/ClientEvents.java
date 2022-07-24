package org.teacon.powertool.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.menu.PowerToolMenus;
import org.teacon.powertool.network.capability.Permission;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
public class ClientEvents {

    @SubscribeEvent
    public static void on(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof AbstractClientPlayer) {
            event.addCapability(Permission.KEY, new Permission.Provider());
        }
    }

    @SubscribeEvent
    public static void on(ScreenOpenEvent event) {
        if (event.getScreen() instanceof CommandBlockEditScreen screen
            && screen.autoCommandBlock instanceof PeriodicCommandBlockEntity blockEntity) {
            event.setScreen(new PeriodicCommandBlockEditScreen(blockEntity));
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = PowerTool.MODID)
    public static final class OnModBus {
        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(PowerToolMenus.POWER_SUPPLY_MENU.get(), PowerSupplyScreen::new);
                ItemBlockRenderTypes.setRenderLayer(PowerToolBlocks.ITEM_SUPPLIER.get(), RenderType.cutout());
            });
        }
        @SubscribeEvent
        public static void ber(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(PowerToolBlocks.ITEM_SUPPLIER_BLOCK_ENTITY.get(), ItemSupplierBlockEntityRenderer::new);
        }
    }
}
