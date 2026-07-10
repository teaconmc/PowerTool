package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.inspection.constraint.NumberConstraint;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

@ParametersAreNonnullByDefault
public class NumberInputField extends AbstractWidget {

    public static final int PADDING             = 12;

    private final Font                          font;
    private final TextField                     textField;
    private final NumberConstraint<?>           constraint;
    private double                              number;

    private @NonNull  DoubleFunction<String>    formatter = Double::toString;
    private @Nullable DoubleConsumer            responder;

    public NumberInputField(
            final Font font,
            final int x,
            final int y,
            final int width,
            final int height,
            final Component message,
            final NumberConstraint<?> constraint
    ) {
        super(x, y, Math.max(width, PADDING * 3), height, message);
        this.font       = font;
        this.constraint = constraint;
        this.textField  = new TextField(font, x + PADDING, y, width - PADDING * 2, height, message);
        this.textField.setConfirm(this::apply);
        this.textField.setMaxLength(20);
    }

    private boolean apply(String string) {
        if (!this.constraint.isValid(string)) {
            this.textField.setValue(this.formatter.apply(this.number), false);
            this.textField.moveCursorToStart(false);
            return false;
        }

        try {
            this.number = Double.parseDouble(string);
            this.writeBack();
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected void extractWidgetRenderState(
            final GuiGraphicsExtractor  graphics,
            final int                   mouseX,
            final int                   mouseY,
            final float                 partialTick
    ) {

        final var width     = this.getWidth();
        final var height    = this.getHeight();
        final var left      = this.getX();
        final var top       = this.getY();
        final var right     = this.getX() + width;
        final var bottom    = this.getY() + height;

        this.isHovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;

        graphics.fill(left, top, right, bottom, 0x88000000);



        if (this.isHovered) {
            final var arrowTop = top + height / 2 - 4;
            graphics.text(
                    this.font,
                    "<",
                    left + 4,
                    arrowTop,
                    mouseX < left + PADDING ? 0xffffffff : 0x40ffffff,
                    false
            );
            graphics.text(
                    this.font,
                    ">",
                    right - 8,
                    arrowTop,
                    mouseX > right - PADDING ? 0xffffffff : 0x40ffffff,
                    false
            );

            if (!this.isFocused()) {
                graphics.outline(
                        left, top,
                        width, height,
                        0xffffffff
                );
            }
        }
        graphics.outline(
                left, top,
                width, height,
                this.isFocused() ? 0xffbbbbff : 0x40ffffff
        );

        this.textField.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);

    }

    @Override
    public void onClick(
            final MouseButtonEvent  event,
            final boolean           doubleClick
    ) {

        final var left  = this.getX();
        final var mx    = event.x();
        final var inc   = event.hasShiftDown() ? 0.25f : (event.hasControlDown() ? 0.1f : 1.0f);

        if (mx < left + PADDING) {
            final var value = this.number;
            this.setNumber(value - inc);
            return;
        } else if (mx > left + this.width - PADDING) {
            final var value = this.number;
            this.setNumber(value + 1);
            return;
        }

        if (doubleClick) {
            this.textField.setFocused(true);
        }

        if (this.textField.isFocused()) {
            this.textField.onClick(event, doubleClick);
        }
    }

    @Override
    public void onRelease(final MouseButtonEvent event) {
        this.textField.onRelease(event);
    }

    @Override
    protected void onDrag(
            final MouseButtonEvent event,
            final double deltaX,
            final double deltaY
    ) {
        if (!this.isFocused()) {
            return;
        }

        final var value = this.number;
        this.textField.setFocused(false);
        if (deltaX > 0 && value < this.constraint.max()) {
            final var result = Math.min((float) (value + deltaX), this.constraint.max());
            this.setNumber(result);
        } else if (deltaX < 0 && value > this.constraint.min()) {
            final var result = Math.max((float) (value + deltaX), this.constraint.min());
            this.setNumber(result);
        }
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        return this.textField.keyPressed(event);
    }

    @Override
    public boolean keyReleased(final KeyEvent event) {
        return this.textField.keyReleased(event);
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        return this.textField.charTyped(event);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput output) {
        this.textField.updateWidgetNarration(output);
    }

    @Override
    public void setFocused(final boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.textField.setFocused(false);
        }
    }

    @Override
    public void setSize(final int width, final int height) {
        super.setSize(width, height);
        this.textField.setSize(width - PADDING * 2, height);
    }

    @Override
    public void setWidth(final int width) {
        super.setWidth(width);
        this.textField.setWidth(width - PADDING * 2);
    }

    @Override
    public void setX(final int x) {
        super.setX(x);
        this.textField.setX(x + PADDING);
    }

    @Override
    public void setPosition(final int x, final int y) {
        super.setPosition(x, y);
        this.textField.setPosition(x + PADDING, y);
    }

    public void setNumber(double number) {
        this.number = number;
        this.textField.setValue(this.formatter.apply(number), false);
        this.textField.moveCursorToStart(false);
    }

    public double getNumber() {
        return this.number;
    }

    public void setFormatter(final DoubleFunction<String> formatter) {
        this.formatter = formatter;
    }

    public void setResponder(final DoubleConsumer responder) {
        this.responder = responder;
    }

    private void writeBack() {
        if (this.responder != null) {
            this.responder.accept(this.number);
        }
    }
}
