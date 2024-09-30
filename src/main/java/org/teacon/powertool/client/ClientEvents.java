package org.teacon.powertool.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.block.entity.RegisterBlockEntity;
import org.teacon.powertool.client.gui.RegisterScreen;
import org.teacon.powertool.client.gui.TrashCanWithContainerScreen;
import org.teacon.powertool.client.renders.FenceKnotRenderer;
import org.teacon.powertool.client.renders.TempleRenderer;
import org.teacon.powertool.client.renders.holo_sign.HolographicSignBlockEntityRenderer;
import org.teacon.powertool.client.renders.ItemDisplayBlockEntityRenderer;
import org.teacon.powertool.client.renders.ItemSupplierBlockEntityRenderer;
import org.teacon.powertool.client.gui.PeriodicCommandBlockEditScreen;
import org.teacon.powertool.client.gui.PowerSupplyScreen;
import org.teacon.powertool.client.renders.holo_sign.LinkHolographicSignBlockEntityRenderer;
import org.teacon.powertool.client.renders.holo_sign.RawJsonHolographicSignBlockEntityRenderer;
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

    /*
     * The following code is inspired by BookRightClickHandler::onRenderHUD from the Patchouli mod,
     * originally authored by Vazkii, williewillus and other Violet Moon members.
     * You can access the original code through the link below
     * https://github.com/VazkiiMods/Patchouli/blob/1.20.x/Xplat/src/main/java/vazkii/patchouli/client/handler/BookRightClickHandler.java
     */
    static void drawRegisterInfo(Minecraft mc, GuiGraphics guiGraphics, ItemStack requestedItem) {
        Window window = mc.getWindow();
        int x = window.getGuiScaledWidth() / 2;
        int y = window.getGuiScaledHeight() / 2;

        // Render first prompt
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.75F, 0.75F, 1F);
        Component prompt1 = Component.translatable("block.powertool.register.hud.prompt.1").withStyle(ChatFormatting.ITALIC);
        guiGraphics.drawString(mc.font, prompt1, (int) ((x + 8) / 0.75F), (int) (y / 0.75F), 0xB0B0B0, false);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        // Positive z index moves things to front
        guiGraphics.pose().translate(0, 0, 10);
        // This renders the item
        guiGraphics.renderItem(requestedItem, x + 8, y + 10);
        // This render the stack size, as well as the foil effect found in enchanted items
        guiGraphics.renderItemDecorations(mc.font, requestedItem, x + 8, y + 10);
        guiGraphics.pose().popPose();

        // Render item name plus the count
        Component itemDisplayName = requestedItem.getHoverName()
                .copy()
                .withStyle(requestedItem.getRarity().getStyleModifier())
                .append(" × " + requestedItem.getCount());
        guiGraphics.drawString(mc.font, itemDisplayName, x + 28, y + 14, 0xFFFFFF, false);

        // Render the second prompt
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.75F, 0.75F, 1F);
        Component prompt2 = Component.translatable("block.powertool.register.hud.prompt.2", Component.keybind("key.use")).withStyle(ChatFormatting.ITALIC);
        guiGraphics.drawString(mc.font, prompt2, (int) ((x + 8) / 0.75F), (int) ((y + 30) / 0.75F), 0xB0B0B0, false);
        guiGraphics.pose().popPose();
    }

    @EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = PowerTool.MODID)
    public static final class OnModBus {
        @SubscribeEvent
        public static void setup(final RegisterMenuScreensEvent event) {
            event.register(PowerToolMenus.POWER_SUPPLY_MENU.get(), PowerSupplyScreen::new);
            event.register(PowerToolMenus.TRASH_CAN_MENU.get(), TrashCanWithContainerScreen::new);
            event.register(PowerToolMenus.REGISTER_MENU.get(), RegisterScreen::new);
        }
        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(PowerToolEntities.FENCE_KNOT.get(), FenceKnotRenderer::new);
            event.registerEntityRenderer(PowerToolEntities.AUTO_VANISH_BOAT.get(),(c) -> new BoatRenderer(c,false));
            event.registerEntityRenderer(PowerToolEntities.AUTO_VANISH_MINECART.get(), (c) -> new MinecartRenderer<>(c, ModelLayers.MINECART));
            event.registerBlockEntityRenderer(PowerToolBlocks.ITEM_DISPLAY_BLOCK_ENTITY.get(), ItemDisplayBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.ITEM_SUPPLIER_BLOCK_ENTITY.get(), ItemSupplierBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), HolographicSignBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.LINK_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), LinkHolographicSignBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.RAW_JSON_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), RawJsonHolographicSignBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.TEMPLE_BLOCK_ENTITY.get(), TempleRenderer::new);
        }
        @SubscribeEvent
        public static void on(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, ResourceLocation.fromNamespaceAndPath(PowerTool.MODID, "cashier_hud"), (guiGraphics, partialTicks) -> {
                Minecraft mc = Minecraft.getInstance();
                HitResult res = mc.hitResult;
                if (mc.level != null && res instanceof BlockHitResult hit) {
                    BlockEntity be = mc.level.getBlockEntity(hit.getBlockPos());
                    if (be instanceof RegisterBlockEntity theBE && !theBE.itemToAccept.isEmpty()) {
                        drawRegisterInfo(mc, guiGraphics, theBE.itemToAccept);
                    }
                }
            });
        }
    }
}
