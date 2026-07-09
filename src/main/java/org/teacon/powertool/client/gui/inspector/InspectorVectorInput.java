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
import org.teacon.powertool.inspection.constraint.NumberConstraint;
import org.teacon.powertool.inspection.property.NumberProperty;

public class InspectorVectorInput<T extends Number> extends InspectorWidget {

    private final Component             message;

    private final TextField             editBox;
    private final NumberProperty<T>     property;
    private final NumberConstraint<T>   constraint;

    public InspectorVectorInput(
            Component           message,
            NumberProperty<T>   property,
            NumberConstraint<T> constraint
    ) {
        super(36);

        this.message = message;
        this.property = property;
        this.constraint = constraint;
        this.editBox = new TextField(Minecraft.getInstance().font, 0, 16, 100, 20, message);

        this.editBox.setValue(String.valueOf(property.get()), false);
        this.editBox.setResponder(this::apply);
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
        graphics.text(font, this.message, 0, 4, 0xffffffff);

        graphics.fill(0, 16, this.editBox.getWidth(), 16 + this.editBox.getHeight(), 0x88000000);
        if (hovered && mouseY > 15) {
            graphics.outline(0, 16, this.editBox.getWidth(), this.editBox.getHeight(), 0xffffffff);
        } else if (this.editBox.isFocused()) {
            graphics.outline(0, 16, this.editBox.getWidth(), this.editBox.getHeight(), 0xffbbbbff);
        }
        this.editBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onMouseDragging(
            final MouseButtonEvent event,
            final double deltaX,
            final double deltaY
    ) {
        if (!this.isFocused()) {
            return;
        }

        final var value = this.property.getNumber();
        this.editBox.setFocused(false);
        if (deltaX > 0 && value < this.constraint.max()) {
            final var result = Math.min((float) (value + deltaX), this.constraint.max());
            this.property.setNumber(result);
            this.editBox.setValue(String.valueOf(result), false);
        } else if (deltaX < 0 && value > this.constraint.min()) {
            final var result = Math.max((float) (value + deltaX), this.constraint.min());
            this.property.setNumber(result);
            this.editBox.setValue(String.valueOf(result), false);
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.editBox.setFocused(false);
        }
    }

    @Override
    public void onMousePressed(final MouseButtonEvent event, final boolean doubleClick) {
        if (doubleClick) {
            this.editBox.setFocused(true);
        }

        if (this.editBox.isFocused()) {
            this.editBox.mouseClicked(event, doubleClick);
        }
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

    private void apply(final String string) {
        if (!this.constraint.isValid(string)) {
            this.editBox.setValue(String.valueOf(this.property.get()), false);
            this.editBox.moveCursorToStart(false);
            return;
        }

        this.property.set(this.constraint.parse(string));
    }
}
