package org.teacon.powertool.client.gui.inspector;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public abstract class InspectorWidget {
    private final int height;

    private boolean focused = false;

    protected InspectorWidget(int height) {
        this.height = height;
    }

    public void render(GuiGraphicsExtractor graphics, int width, int mouseX, int mouseY, float partialTick, boolean hovered) {}

    public void onMousePressed(MouseButtonEvent event, boolean doubleClick, int width) {}

    public void onMouseReleased(MouseButtonEvent event, int width) {}

    public void onMouseDragging(MouseButtonEvent event, double deltaX, double deltaY, int width) {}

    public boolean onKeyPressed(KeyEvent event) { return false; }

    public boolean onKeyReleased(KeyEvent event) { return false; }

    public boolean charTyped(CharacterEvent event) { return false; }

    public boolean paste(InspectorWidget copy) { return false; }

    public int getHeight() {
        return this.height;
    }

    public boolean isFocused() {
        return this.focused;
    }


    public void setFocused(final boolean focused) {
        this.focused = focused;
    }
}
