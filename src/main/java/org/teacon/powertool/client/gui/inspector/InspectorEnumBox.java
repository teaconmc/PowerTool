package org.teacon.powertool.client.gui.inspector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.inspection.DisplayableType;
import org.teacon.powertool.inspection.Duplicatable;
import org.teacon.powertool.inspection.property.Property;

public class InspectorEnumBox<T extends DisplayableType> extends InspectorModificationWidget<T> {
    private final T[] values;

    private boolean dropped = false;

    private T selected;

    public InspectorEnumBox(Component message, Property<T> property, T[] values) {
        super(22, message, property);
        this.values = values;
        this.selected = property.get();
    }

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, boolean hovered) {

        int width = this.getWidth();

        int buttonWidth = width / 2;
        int left = width - buttonWidth;

        graphics.textWithWordWrap(Minecraft.getInstance().font, this.message, 0, 8, left, 0xffffffff);

        int hover = hovered ? mouseY / 20 : -1;
        graphics.fill(left, 2, width, 20, 0xbb000000);
        if (this.dropped) {
            graphics.outline(left, 2, buttonWidth, 20, 0xffbbbbff);
        } else if (hover == 0) {
            graphics.outline(left, 2, buttonWidth, 20, 0xffffffff);
        }

        int half = buttonWidth / 2;
        graphics.centeredText(
                Minecraft.getInstance().font,
                this.selected.getDisplayName(),
                left + half, 8,
                0xffffffff
        );

        if (this.dropped) {
            for (int i = 0; i < values.length; i++) {
        
                graphics.fill(left, 22 + 20 * i, width, 42 + 20 * i, 0xbb000000);
                if (hover == i + 1) {
                    graphics.outline(left, 22 + 20 * i, buttonWidth, 20, 0xffffffff);
                }

                graphics.centeredText(
                        Minecraft.getInstance().font,
                        this.values[i].getDisplayName(),
                        left + half, 28 + 20 * i,
                        0xffffffff
                );
            }
        }
    }

    @Override
    public void onMouseReleased(final MouseButtonEvent event) {

        var button = event.button();
        var mouseY = event.y();

        if (button != 0) {
            return;
        }

        if (mouseY > 20)  {
            int i = Math.clamp((int) (mouseY / 20) - 1, 0, 1);
            T value = this.values[i];

            if (value != this.selected) {
                this.selected = value;
                this.property.set(value);
            }
        }

        this.dropped = !this.dropped;
    }

    @Override
    public int getHeight() {
        return this.dropped ? 24 + this.values.length * 20 : 24;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void paste(@NonNull Duplicatable copy) {
        if (copy.getClass() == InspectorEnumBox.class) {
            final var box = (InspectorEnumBox<?>) copy;
            if (box.selected.getClass() == this.selected.getClass()) {
                this.selected = (T) box.selected;
                this.property.set(this.selected);
            }
        }
    }
}
