package org.teacon.powertool.client.gui.inspector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.teacon.powertool.inspection.property.Property;

public class InspectorCheckBox extends InspectorModificationWidget<Boolean> {

    private int left;

    public InspectorCheckBox(
            final Component         message,
            final Property<Boolean> property
    ) {
        super(20, message, property);
    }

    @Override
    public void render(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick, final boolean hovered) {

        final var font = Minecraft.getInstance().font;

        graphics.text(font, this.message, 0, 6, 0xffffffff);
        graphics.outline(this.left, 5, 10, 10, hovered ? 0xffbbbbff : 0xffffffff);

        final boolean active = this.property.get();

        if (active) {
            graphics.fill(
                    this.left + 3, 8,
                    this.left + 7, 12,
                    hovered ? 0xffbbbbff : 0xffffffff
            );
        } else if (hovered) {
            graphics.fill(
                    this.left + 3, 8,
                    this.left + 7, 12,
                    0xffffffff
            );
        }

    }

    @Override
    public void onMousePressed(final MouseButtonEvent event, final boolean doubleClick) {
        final var active = this.property.get();
        this.property.set(!active);
        Minecraft   .getInstance()
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void resize(final int width) {
        super.resize(width);

        this.left = width - 20;
    }
}
