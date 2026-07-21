package org.teacon.powertool.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugEntryLookingAt;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.client.anvilcraft.rendering.CacheableBERenderingPipeline;
import org.teacon.powertool.client.gui.JEIRecipeDisplayScreen;
import org.teacon.powertool.client.gui.PeriodicCommandBlockEditScreen;
import org.teacon.powertool.client.gui.PowerSupplyScreen;
import org.teacon.powertool.client.gui.RegisterScreen;
import org.teacon.powertool.client.gui.TextureExtractorScreen;
import org.teacon.powertool.client.gui.TrashCanWithContainerScreen;
import org.teacon.powertool.client.renders.BezierCurveRenderingPipeline;
import org.teacon.powertool.client.renders.ItemDisplayBlockEntityRenderer;
import org.teacon.powertool.client.renders.ItemSupplierBlockEntityRenderer;
import org.teacon.powertool.client.renders.JEIRecipeDisplayBlockEntityRenderer;
import org.teacon.powertool.client.renders.TempleRenderer;
import org.teacon.powertool.client.renders.BezierCurveBlockRenderer;
import org.teacon.powertool.client.renders.entity.AutoVanishBoatRenderer;
import org.teacon.powertool.client.renders.entity.FenceKnotRenderer;
import org.teacon.powertool.client.renders.entity.MartingCarEntityRenderer;
import org.teacon.powertool.client.renders.entity.model.MartingCarEntityModel;
import org.teacon.powertool.client.renders.holo_sign.HolographicSignBlockEntityRenderer;
import org.teacon.powertool.client.renders.holo_sign.LinkHolographicSignBlockEntityRenderer;
import org.teacon.powertool.client.renders.holo_sign.RawJsonHolographicSignBlockEntityRenderer;
import org.teacon.powertool.client.renders.item.CommandRuneSpecialRenderer;
import org.teacon.powertool.entity.MartingCarEntity;
import org.teacon.powertool.entity.PowerToolEntities;
import org.teacon.powertool.menu.PowerToolMenus;
import org.teacon.powertool.network.server.UndoCreativeBlockBreakPacket;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.List;

@NonNullByDefault
@EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
public class PowerToolClientEvents {

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

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getAction() == InputConstants.PRESS && event.getKey() == GLFW.GLFW_KEY_Z
                && (event.getModifiers() & InputConstants.MOD_CONTROL) != 0
                && minecraft.screen == null
                && minecraft.player != null
                && minecraft.player.isCreative()) {
            ClientPacketDistributor.sendToServer(UndoCreativeBlockBreakPacket.INSTANCE);
        }
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
    public static Vector2i drawRegisterInfo(
        Minecraft mc,
        GuiGraphicsExtractor guiGraphics,
        ItemStack item,
        int xOffset,
        int yOffset,
        Component componentTop,
        Component componentBottom
    ) {
        Window window = mc.getWindow();
        int x = window.getGuiScaledWidth() / 2 + xOffset;
        int y = window.getGuiScaledHeight() / 2 + yOffset;

        if (!componentTop.getString().isEmpty()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().scale(0.75F, 0.75F);
            guiGraphics.text(mc.font, componentTop, (int) ((x + 8) / 0.75F), (int) (y / 0.75F), 0xFFB0B0B0, false);
            guiGraphics.pose().popMatrix();
        }

        guiGraphics.item(item, x + 8, y + 10);
        guiGraphics.itemDecorations(mc.font, item, x + 8, y + 10);

        Component itemDisplayName = item.getHoverName()
            .copy()
            .withStyle(item.getRarity().getStyleModifier())
            .append(" × " + item.getCount());
        guiGraphics.text(mc.font, itemDisplayName, x + 28, y + 14, 0xFFFFFFFF, false);

        if (!componentBottom.getString().isEmpty()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().scale(0.75F, 0.75F);
            guiGraphics.text(
                mc.font,
                componentBottom,
                (int) ((x + 8) / 0.75F),
                (int) ((y + 30) / 0.75F),
                0xFFB0B0B0,
                false
            );
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
        CreativeNoClipClient.setEnabled(false);
    }

    @SubscribeEvent
    static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        var p1 = event.getOldPlayer();
        var p2 = event.getNewPlayer();
        if (p1.level().dimension() != p2.level().dimension()) AccessControlClient.INSTANCE.clear();
    }

    @SubscribeEvent
    public static void onRegDebugOverlayEntry(RegisterDebugEntriesEvent event) {
        event.register(
            VanillaUtils.modRL("block_access_control_mode"), new DebugEntryLookingAt() {
                @Override
                public HitResult getHitResult(Entity cameraEntity) {
                    return cameraEntity.pick(20.0, 0.0F, false);
                }

                @Override
                public void extractInfo(List<String> result, Level level, BlockPos pos) {
                    boolean isDisplayModeEnabled = AccessControlClient.INSTANCE.isDisplayModeEnabledAt(pos);
                    boolean isCachedModeEnabled = CachedModeClient.INSTANCE.isCachedModeEnabledOn(pos);
                    result.add(
                        "Display Mode: "
                            + (isDisplayModeEnabled ? ChatFormatting.GREEN + "Enabled" : ChatFormatting.RED + "Disabled")
                    );
                    result.add(
                        "Cached Mode: "
                            + ((isCachedModeEnabled) ? ChatFormatting.GREEN + "Enabled" : ChatFormatting.RED + "Disabled")
                    );
                }

                @Override
                public Identifier group() {
                    return DebugEntryLookingAt.BLOCK_GROUP;
                }
            }
        );
    }

    @SubscribeEvent
    public static void on(RenderFrameEvent.Pre event) {
        if (CacheableBERenderingPipeline.getInstance() != null) {
            CacheableBERenderingPipeline.getInstance().runTasks();
        }
        var bezierCurveRenderingPipeline = BezierCurveRenderingPipeline.getInstance();
        if (bezierCurveRenderingPipeline != null) {
            bezierCurveRenderingPipeline.runTasks();
        }
    }

    @SubscribeEvent
    public static void on(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        if (CacheableBERenderingPipeline.getInstance() != null) {
            CacheableBERenderingPipeline.getInstance().render(
                event.getLevelRenderState().cameraRenderState.cullFrustum,
                false
            );
        }
        var bezierCurveRenderingPipeline = BezierCurveRenderingPipeline.getInstance();
        if (bezierCurveRenderingPipeline != null) {
            bezierCurveRenderingPipeline.render();
        }
    }

    @SubscribeEvent
    public static void on(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        if (CacheableBERenderingPipeline.getInstance() != null) {
            CacheableBERenderingPipeline.getInstance().render(
                event.getLevelRenderState().cameraRenderState.cullFrustum,
                true
            );
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
    public static final class OnModBus {
        @SubscribeEvent
        public static void setup(final RegisterMenuScreensEvent event) {
            event.register(PowerToolMenus.POWER_SUPPLY_MENU.get(), PowerSupplyScreen::new);
            event.register(PowerToolMenus.TRASH_CAN_MENU.get(), TrashCanWithContainerScreen::new);
            event.register(PowerToolMenus.REGISTER_MENU.get(), RegisterScreen::new);
            event.register(PowerToolMenus.TEXTURE_EXTRACTOR_MENU.get(), TextureExtractorScreen::new);
            event.register(PowerToolMenus.JEI_RECIPE_DISPLAY_MENU.get(), JEIRecipeDisplayScreen::new);
        }

        @SubscribeEvent
        public static void specialModelRenderers(RegisterSpecialModelRendererEvent event) {
            event.register(VanillaUtils.modRL("command_rune"), CommandRuneSpecialRenderer.Unbaked.MAP_CODEC);
        }

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(
                PowerToolBlocks.ITEM_DISPLAY_BLOCK_ENTITY.get(),
                ItemDisplayBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(
                PowerToolBlocks.ITEM_SUPPLIER_BLOCK_ENTITY.get(),
                ItemSupplierBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(
                PowerToolBlocks.HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(),
                HolographicSignBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(
                PowerToolBlocks.LINK_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(),
                LinkHolographicSignBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(
                PowerToolBlocks.RAW_JSON_HOLOGRAPHIC_SIGN_BLOCK_ENTITY.get(),
                RawJsonHolographicSignBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(PowerToolBlocks.TEMPLE_BLOCK_ENTITY.get(), TempleRenderer::new);
            event.registerBlockEntityRenderer(
                PowerToolBlocks.JEI_RECIPE_DISPLAY_BLOCK_ENTITY.get(),
                JEIRecipeDisplayBlockEntityRenderer::new
            );
//            event.registerBlockEntityRenderer(PowerToolBlocks.BEZIER_CURVE_BLOCK_ENTITY.get(), BezierCurveBlockRenderer::new);
            event.registerBlockEntityRenderer(
                PowerToolBlocks.BEZIER_CURVE_BLOCK_ENTITY.get(),
                BezierCurveBlockRenderer::new
            );

            event.registerEntityRenderer(PowerToolEntities.MARTING.get(), MartingCarEntityRenderer::new);
            event.registerEntityRenderer(PowerToolEntities.FENCE_KNOT.get(), FenceKnotRenderer::new);
            event.registerEntityRenderer(PowerToolEntities.AUTO_VANISH_BOAT.get(), AutoVanishBoatRenderer::new);
            event.registerEntityRenderer(
                PowerToolEntities.AUTO_VANISH_MINECART.get(),
                (c) -> new MinecartRenderer(c, ModelLayers.MINECART)
            );
        }

        @SubscribeEvent
        public static void onRegModelLayerDef(EntityRenderersEvent.RegisterLayerDefinitions event) {
            for (var v : MartingCarEntity.Variant.values()) {
                event.registerLayerDefinition(
                    MartingCarEntityRenderer.getModelLayer(v),
                    MartingCarEntityModel::createBodyLayer
                );
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onRegClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerFluidType(
                new IClientFluidTypeExtensions() {
                    @Override
                    public Identifier getRenderOverlayTexture(Minecraft mc) {
                        return Identifier.withDefaultNamespace("textures/misc/underwater.png");
                    }
                }, PowerToolBlocks.FAKE_WATER_TYPE.get()
            );
        }

        @SubscribeEvent
        public static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
            event.register(
                new FluidModel.Unbaked(
                    new Material(Identifier.withDefaultNamespace("block/water_still"), false),
                    new Material(Identifier.withDefaultNamespace("block/water_flow"), false),
                    null,
                    FluidTintSources.water()
                ), PowerToolBlocks.FAKE_WATER
            );
        }

    }
}
