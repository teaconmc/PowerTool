package org.teacon.powertool.client.gui.inspector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.client.gui.widget.NumberInputField;
import org.teacon.powertool.inspection.constraint.NumberConstraint;
import org.teacon.powertool.inspection.property.NumberProperty;

public class InspectorNumberInput<T extends Number> extends InspectorWidget {

    private final Component             message;

    private final NumberInputField      input;
    private final NumberProperty<T>     property;

    public InspectorNumberInput(
            Component           message,
            NumberProperty<T>   property,
            NumberConstraint<T> constraint
    ) {
        super(20);

        this.message = message;
        this.property = property;
        
        this.input = new NumberInputField(
                Minecraft.getInstance().font,
                50,
                0,
                50,
                20,
                Component.empty(),
                constraint
        );
        
        this.input.setNumber(property.getNumber());
        this.input.setResponder(this::apply);
    }

    private void apply(double v) {
        this.property.setNumber(v);
    }

    @Override
    public void render(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            boolean hovered
    ) {
        Font font = Minecraft.getInstance().font;
        graphics.text(font, this.message, 0, 6, 0xffffffff);

        this.input.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onMouseDragging(
            final MouseButtonEvent event,
            final double deltaX,
            final double deltaY
    ) {
        this.input.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.input.setFocused(false);
        }
    }

    @Override
    public void onMousePressed(final MouseButtonEvent event, final boolean doubleClick) {
        this.input.setFocused(this.input.isHovered());
        this.input.mouseClicked(event, doubleClick);
    }

    @Override
    public void onMouseReleased(final MouseButtonEvent event) {
        this.input.mouseReleased(event);
    }

    @Override
    public boolean onKeyPressed(final KeyEvent event) {
        return this.input.keyPressed(event);
    }

    @Override
    public boolean onKeyReleased(final KeyEvent event) {
        return this.input.keyReleased(event);
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        return this.input.charTyped(event);
    }

    @Override
    public void resize(final int width) {
        super.resize(width);
        final var half = width / 2;
        this.input.setX(half);
        this.input.setWidth(width - half);
    }
}
