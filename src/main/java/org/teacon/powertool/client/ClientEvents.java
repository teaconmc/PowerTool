package org.teacon.powertool.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.client.fluid.WrappedClientFluidTypeExtension;
import org.teacon.powertool.client.gui.PeriodicCommandBlockEditScreen;
import org.teacon.powertool.client.gui.PowerSupplyScreen;
import org.teacon.powertool.client.gui.RegisterScreen;
import org.teacon.powertool.client.gui.TextureExtractorScreen;
import org.teacon.powertool.client.gui.TrashCanWithContainerScreen;
import org.teacon.powertool.client.renders.BezierCurveBlockRenderer;
import org.teacon.powertool.client.renders.ItemDisplayBlockEntityRenderer;
import org.teacon.powertool.client.renders.ItemSupplierBlockEntityRenderer;
import org.teacon.powertool.client.renders.TempleRenderer;
import org.teacon.powertool.client.renders.entity.FenceKnotRenderer;
import org.teacon.powertool.client.renders.entity.MartingCarEntityRenderer;
import org.teacon.powertool.client.renders.entity.model.MartingCarEntityModel;
import org.teacon.powertool.client.renders.holo_sign.HolographicSignBlockEntityRenderer;
import org.teacon.powertool.client.renders.holo_sign.LinkHolographicSignBlockEntityRenderer;
import org.teacon.powertool.client.renders.holo_sign.RawJsonHolographicSignBlockEntityRenderer;
import org.teacon.powertool.entity.MartingCarEntity;
import org.teacon.powertool.entity.PowerToolEntities;
import org.teacon.powertool.menu.PowerToolMenus;

@EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
public class ClientEvents {
    
    public static int tickCount = 0;
    
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof CommandBlockEditScreen screen
                && screen.autoCommandBlock instanceof PeriodicCommandBlockEntity blockEntity) {
            event.setNewScreen(new PeriodicCommandBlockEditScreen(blockEntity));
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        tickCount++;
    }
    
    
    /**
     * The following code is inspired by BookRightClickHandler::onRenderHUD from the Patchouli mod,
     * originally authored by Vazkii, williewillus and other Violet Moon members.
     * You can access the original code through the link
     * <a href="https://github.com/VazkiiMods/Patchouli/blob/1.20.x/Xplat/src/main/java/vazkii/patchouli/client/handler/BookRightClickHandler.java">here</a>
     *
     * @return The lower right pos of rendered area.
     */
    @SuppressWarnings("SameParameterValue")
    public static Vector2i drawRegisterInfo(Minecraft mc, GuiGraphicsExtractor guiGraphics, ItemStack item, int xOffset, int yOffset, Component componentTop, Component componentBottom) {
        Window window = mc.getWindow();
        int x = window.getGuiScaledWidth() / 2 + xOffset;
        int y = window.getGuiScaledHeight() / 2 + yOffset;
        
        if (!componentTop.getString().isEmpty()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().scale(0.75F, 0.75F);
            guiGraphics.text(mc.font, componentTop, (int) ((x + 8) / 0.75F), (int) (y / 0.75F), 0xB0B0B0, false);
            guiGraphics.pose().popMatrix();
        }
        
        guiGraphics.item(item, x + 8, y + 10);
        guiGraphics.itemDecorations(mc.font, item, x + 8, y + 10);
        
        Component itemDisplayName = item.getHoverName()
                .copy()
                .withStyle(item.getRarity().getStyleModifier())
                .append(" × " + item.getCount());
        guiGraphics.text(mc.font, itemDisplayName, x + 28, y + 14, 0xFFFFFF, false);
        
        if (!componentBottom.getString().isEmpty()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().scale(0.75F, 0.75F);
            guiGraphics.text(mc.font, componentBottom, (int) ((x + 8) / 0.75F), (int) ((y + 30) / 0.75F), 0xB0B0B0, false);
            guiGraphics.pose().popMatrix();
        }
        
        var xSize = xOffset + 28 + mc.font.width(itemDisplayName);
        var ySize = yOffset + 40;
        return new Vector2i(xSize, ySize);
    }
    
    @SubscribeEvent
    static void onMousePress(ScreenEvent.MouseButtonPressed.Pre event) {
        event.setCanceled(AccessControlClient.INSTANCE.isDisplayModeEnabledOn(event.getScreen()));
    }
    
    @SubscribeEvent
    static void onMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        event.setCanceled(AccessControlClient.INSTANCE.isDisplayModeEnabledOn(event.getScreen()));
    }
    
    @SubscribeEvent
    static void onKeyPress(ScreenEvent.KeyPressed.Pre event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_ESCAPE) {
            event.setCanceled(AccessControlClient.INSTANCE.isDisplayModeEnabledOn(event.getScreen()));
        }
    }
    
    @SubscribeEvent
    static void onKeyRelease(ScreenEvent.KeyReleased.Pre event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_ESCAPE) {
            event.setCanceled(AccessControlClient.INSTANCE.isDisplayModeEnabledOn(event.getScreen()));
        }
    }
    
    @SubscribeEvent
    static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        event.setCanceled(AccessControlClient.INSTANCE.isDisplayModeEnabledOn(event.getScreen()));
    }
    
    @SubscribeEvent
    static void onScreenClosing(ScreenEvent.Closing event) {
        AccessControlClient.INSTANCE.screenClosed();
    }
    
    @SubscribeEvent
    static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        AccessControlClient.INSTANCE.clear();
    }
    
    @SubscribeEvent
    static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        var p1 = event.getOldPlayer();
        var p2 = event.getNewPlayer();
        if (p1.level().dimension() != p2.level().dimension()) AccessControlClient.INSTANCE.clear();
    }
    
    @EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
    public static final class OnModBus {
        @SubscribeEvent
        public static void setup(final RegisterMenuScreensEvent event) {
            event.register(PowerToolMenus.POWER_SUPPLY_MENU.get(), PowerSupplyScreen::new);
            event.register(PowerToolMenus.TRASH_CAN_MENU.get(), TrashCanWithContainerScreen::new);
            event.register(PowerToolMenus.REGISTER_MENU.get(), RegisterScreen::new);
            event.register(PowerToolMenus.TEXTURE_EXTRACTOR_MENU.get(), TextureExtractorScreen::new);
        }
        
        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(PowerToolBlocks.ITEM_DISPLAY_BLOCK_ENTITY.get(), ItemDisplayBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.ITEM_SUPPLIER_BLOCK_ENTITY.get(), ItemSupplierBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), HolographicSignBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.LINK_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), LinkHolographicSignBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.RAW_JSON_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(), RawJsonHolographicSignBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.TEMPLE_BLOCK_ENTITY.get(), TempleRenderer::new);
            event.registerBlockEntityRenderer(PowerToolBlocks.BEZIER_CURVE_BLOCK_ENTITY.get(), BezierCurveBlockRenderer::new);
            
            event.registerEntityRenderer(PowerToolEntities.MARTING.get(), MartingCarEntityRenderer::new);
            event.registerEntityRenderer(PowerToolEntities.FENCE_KNOT.get(), FenceKnotRenderer::new);
            event.registerEntityRenderer(PowerToolEntities.AUTO_VANISH_BOAT.get(), (c) -> new BoatRenderer(c, false));
            event.registerEntityRenderer(PowerToolEntities.AUTO_VANISH_MINECART.get(), (c) -> new MinecartRenderer<>(c, ModelLayers.MINECART));
        }
        
        @SubscribeEvent
        public static void onRegModelLayerDef(EntityRenderersEvent.RegisterLayerDefinitions event) {
            for (var v : MartingCarEntity.Variant.values()) {
                event.registerLayerDefinition(MartingCarEntityRenderer.getModelLayer(v), MartingCarEntityModel::createBodyLayer);
            }
        }
        
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onRegClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerFluidType(new WrappedClientFluidTypeExtension(IClientFluidTypeExtensions.of(NeoForgeMod.WATER_TYPE.value())), PowerToolBlocks.FAKE_WATER_TYPE.get());
        }
        
    }
}
