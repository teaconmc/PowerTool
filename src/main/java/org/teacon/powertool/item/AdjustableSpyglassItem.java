package org.teacon.powertool.item;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.SpyglassItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.annotation.NonNullByDefault;

@NonNullByDefault
public class AdjustableSpyglassItem extends SpyglassItem {
    public AdjustableSpyglassItem(Properties properties) {
        super(properties);
    }

    @NonNullByDefault
    @EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
    public static final class ClientEvents {
        private static final float MIN_FOV_MODIFIER = 0.01F;
        private static final float MAX_FOV_MODIFIER = 1.0F;
        private static final float FOV_STEP = 0.01F;
        private static float fovModifier = ZOOM_FOV_MODIFIER;

        private ClientEvents() {
        }

        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            if (!isUsingAdjustableSpyglass() || event.getScrollDeltaY() == 0.0) {
                return;
            }
            float direction = event.getScrollDeltaY() > 0.0 ? -1.0F : 1.0F;
            fovModifier = Mth.clamp(fovModifier + direction * FOV_STEP, MIN_FOV_MODIFIER, MAX_FOV_MODIFIER);
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onComputeFov(ViewportEvent.ComputeFov event) {
            if (isUsingAdjustableSpyglass()) {
                event.setFOV(event.getFOV() * fovModifier / ZOOM_FOV_MODIFIER);
            }
        }

        @SubscribeEvent
        public static void onRenderCameraOverlays(RenderGuiLayerEvent.Pre event) {
            if (event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS) && isUsingAdjustableSpyglass()) {
                event.setCanceled(true);
            }
        }

        private static boolean isUsingAdjustableSpyglass() {
            var minecraft = Minecraft.getInstance();
            var player = minecraft.player;
            return minecraft.options.getCameraType().isFirstPerson()
                    && player != null
                    && player.isUsingItem()
                    && player.getUseItem().getItem() instanceof AdjustableSpyglassItem;
        }
    }
}
