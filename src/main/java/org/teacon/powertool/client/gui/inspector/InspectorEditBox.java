package org.teacon.powertool.client.gui.inspector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.client.gui.widget.TextField;
import org.teacon.powertool.inspection.property.Property;

public class InspectorEditBox extends InspectorModificationWidget<String> {
    private final TextField editBox;

    public InspectorEditBox(Component message, Property<String> property) {
        super(36, message, property);

        this.editBox = new TextField(Minecraft.getInstance().font, 0, 16, 100, 20, message);
        this.editBox.setValue(property.get(), false);
        this.editBox.setResponder(property::set);
    }

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, boolean hovered) {
        Font font = Minecraft.getInstance().font;
        graphics.text(font, this.message, 0, 4, 0xffffffff);

        graphics.fill(0, 16, this.editBox.getWidth(), 16 + this.editBox.getHeight(), 0x88000000);
        if (this.editBox.isMouseOver(mouseX, mouseY)) {
            graphics.outline(0, 16, this.editBox.getWidth(), this.editBox.getHeight(), 0xffffffff);
        } else if (this.isFocused()) {
            graphics.outline(0, 16, this.editBox.getWidth(), this.editBox.getHeight(), 0xffbbbbff);
        }
        this.editBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        this.editBox.setFocused(focused);
    }

    @Override
    public void onMousePressed(final MouseButtonEvent event, final boolean doubleClick) {
        this.editBox.mouseClicked(event, doubleClick);
    }

    @Override
    public void onMouseReleased(final MouseButtonEvent event) {
        this.editBox.mouseReleased(event);
    }

    @Override
    public boolean onKeyPressed(final KeyEvent event) {
        return this.editBox.keyPressed(event);
    }

    @Override
    public boolean onKeyReleased(final KeyEvent event) {
        return this.editBox.keyReleased(event);
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        return this.editBox.charTyped(event);
    }

    @Override
    public void resize(final int width) {
        super.resize(width);
        this.editBox.setWidth(width);
    }
}
