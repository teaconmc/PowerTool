package org.teacon.powertool.client.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.WindowResizeEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.client.gui.widget.Inspector;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/*
* 先做着，应该迟早会有其他用处的 [Louis_Quepierts]
* */
@ParametersAreNonnullByDefault
public class InspectorOverlay implements GuiLayer {

    public static final InspectorOverlay    INSTANCE = new InspectorOverlay();

    private @Nullable   Inspector           inspector;

    private int width;
    private int height;

    private InspectorOverlay() {

        final var window    = Minecraft.getInstance().getWindow();
        this.width          = window.getGuiScaledWidth();
        this.height         = window.getGuiScaledHeight();

    }

    @Override
    public void render(
            final GuiGraphicsExtractor  graphics,
            final DeltaTracker          deltaTracker
    ) {

        if (this.inspector == null) {
            return;
        }

        this.inspector.extractRenderState(
                graphics,
                -1, -1,
                deltaTracker.getGameTimeDeltaPartialTick(false)
        );

    }

    public void setInspector(@Nullable final Inspector inspector) {
        this.inspector = inspector;
        this._resize();
    }

    public void cleanup() {
        this.inspector = null;
    }

    private void resize(
            final int width,
            final int height
    ) {

        this.width  = width;
        this.height = height;

        this        ._resize();

    }

    private void _resize() {

        if (this.inspector != null) {
            this.inspector.setHeight(this.height);

            final var left  = this.width - this.inspector.getWidth();
            this.inspector.setX(left);
        }

    }


    @EventBusSubscriber(value = Dist.CLIENT, modid = PowerTool.MODID)
    private static final class Handler {

        @SubscribeEvent
        public static void onResizeWindow(final WindowResizeEvent event) {
            final var window = event.getWindow();
            InspectorOverlay.INSTANCE.resize(
                    window.getGuiScaledWidth(),
                    window.getGuiScaledHeight()
            );
        }

    }

}
