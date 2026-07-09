package org.teacon.powertool.client.gui.inspector;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class InspectorSeparator extends InspectorWidget {

    private final int padding;

    public InspectorSeparator(
            final int top,
            final int bottom
    ) {
        super(top + bottom);
        this.padding = top;
    }

    @Override
    public void render(
            final GuiGraphicsExtractor graphics,
            final int mouseX,
            final int mouseY,
            final float partialTick,
            final boolean hovered
    ) {
        final int left = 10;
        final int right = this.getWidth() - 10;
        graphics.horizontalLine(
                left,
                right,
                2 + padding,
                0xffffffff
        );
    }
}
