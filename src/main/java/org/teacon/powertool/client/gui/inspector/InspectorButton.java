package org.teacon.powertool.client.gui.inspector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class InspectorButton extends InspectorWidget {

    private static final int PADDING = 2;

    private final Component component;
    private final Runnable onClick;

    private boolean hovered;

    protected InspectorButton(
            final Component component,
            final Runnable  onClick
    ) {
        super(24);

        this.component  = component;
        this.onClick    = onClick;
    }

    @Override
    public void render(
            final GuiGraphicsExtractor graphics,
            final int mouseX,
            final int mouseY,
            final float partialTick,
            final boolean hovered
    ) {

        final var font = Minecraft.getInstance().font;


        final var top = PADDING;
        final var bottom = this.getHeight() - PADDING;
        graphics.fill(
                0, PADDING,
                this.getWidth(), bottom,
                0x88000000
        );
        graphics.centeredText(
                font,
                this.component,
                this.getWidth() / 2,
                (this.getHeight() - font.lineHeight) / 2,
                0xffffffff
        );

        this.hovered = mouseY >= top && mouseY < bottom;

        if (this.hovered) {
            graphics.outline(
                    0, top,
                    this.getWidth(), this.getHeight() - PADDING * 2,
                    0xffffffff
            );
        }

    }

    @Override
    public void onMousePressed(final MouseButtonEvent event, final boolean doubleClick) {

        if (this.hovered) {
            this.onClick.run();
            Minecraft   .getInstance()
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

    }
}
