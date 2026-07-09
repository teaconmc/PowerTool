package org.teacon.powertool.client.gui.inspector;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public abstract class InspectorWidget {
    private final int height;
    private int width;

    private boolean focused = false;

    protected InspectorWidget(int height) {
        this.height = height;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, boolean hovered) {}

    public void onMousePressed(MouseButtonEvent event, boolean doubleClick) {}

    public void onMouseReleased(MouseButtonEvent event) {}

    public void onMouseDragging(MouseButtonEvent event, double deltaX, double deltaY) {}

    public boolean onKeyPressed(KeyEvent event) { return false; }

    public boolean onKeyReleased(KeyEvent event) { return false; }

    public boolean charTyped(CharacterEvent event) { return false; }

    public boolean paste(InspectorWidget copy) { return false; }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public boolean isFocused() {
        return this.focused;
    }


    public void setFocused(final boolean focused) {
        this.focused = focused;
    }

    public void resize(final int width) {
        this.width = width;
    }
}
