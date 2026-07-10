package org.teacon.powertool.client.gui.inspector;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.inspection.Duplicatable;
import org.teacon.powertool.inspection.constraint.NumberConstraint;
import org.teacon.powertool.inspection.property.FloatProperty;
import org.teacon.powertool.inspection.property.IntegerProperty;
import org.teacon.powertool.inspection.property.NumberProperty;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public sealed abstract class InspectorSlider<T extends Number> extends InspectorModificationWidget<T> {

    private final float min;
    private final float max;
    private final float step;

    protected float     value;

    protected InspectorSlider(
            final Component             message,
            final NumberProperty<T>     property,
            final NumberConstraint<T>   constraint,
            final float                 step
    ) {
        super(36, message, property);

        this.min                = constraint.min();
        this.max                = constraint.max();

        this.step               = step;
        this.value              = (float) property.getNumber();
    }

    @Override
    public void render(
            GuiGraphicsExtractor    graphics,
            int                     mouseX,
            int                     mouseY,
            float                   partialTick,
            boolean                 hovered
    ) {
        int width           = this.getWidth();

        Font font           = Minecraft.getInstance().font;
        graphics            .text(font, this.message, 0, 4, 0xffffffff);

        String text         = this.getDisplayText(this.value);
        int textWidth       = font.width(text);
        graphics            .text(font, text, width - textWidth, 4, 0xffffffff);


        int length          = width - 10;
        float interpolate   = (this.value - this.min) / (this.max - this.min);
        int offset          = 4 + (int) (length * interpolate);

        boolean hover       = hovered && mouseY > 14;
        graphics            .fill(0, 14, width, 34, 0xbb000000);
        graphics            .fill(4, 23, width - 4, 25, 0x88ffffff);
        graphics            .fill(offset, 17, offset + 2, 31, 0xffffffff);

        if (hover) {
            graphics        .outline(0, 14, width, 20, 0xffffffff);
        }
    }

    @Override
    public void onMousePressed(final MouseButtonEvent event, final boolean doubleClick) {

        var width  = this.getWidth();
        var mouseX = event.x();
        var mouseY = event.y();
        var button = event.button();

        if (button != 0 || mouseY <= 14) {
            return;
        }

        if (mouseX < 8 || mouseX > width - 8) {
            return;
        }

        this.slide(width, (float) mouseX);
    }

    @Override
    public void onMouseDragging(final MouseButtonEvent event, double deltaX, double deltaY) {

        var width  = this.getWidth();
        var mouseX = event.x();
        var button = event.button();

        if (button != 0) {
            return;
        }

        this.slide(width, (float) mouseX);
    }

    @Override
    public void paste(final Duplicatable copy) {
        if (copy instanceof InspectorSlider<?> slider) {
            this.apply(slider.value);
        }
    }

    @Override
    public void resize(final int width) {
        super.resize(width);
    }

    private void slide(final int width, final float mouseX) {
        float interpolate   = Math.clamp((mouseX - 8) / (width - 18), 0.0f, 1.0f);
        float target        = (this.max - this.min) * interpolate + this.min;
        float value         = Math.round(target / this.step) * this.step;

        this                .apply(value);
    }

    protected void apply(final float value) {
        float clamped   = this.clamp(value);

        if (clamped != this.value) {
            this.value = clamped;
            ((NumberProperty<T>) this.property).setNumber(clamped);
        }
    }

    protected String getDisplayText(final float value) {
        return String.format("%.2f", value);
    }

    protected float clamp(final float value) {
        return value;
    }

    public static final class Integer extends InspectorSlider<java.lang.Integer> {
        public Integer(
                final Component                             message,
                final IntegerProperty                       property,
                final NumberConstraint<java.lang.Integer>   constraint,
                final float                                 step
        ) {
            super(message, property, constraint, step);
        }

        @Override
        protected String getDisplayText(final float value) {
            return java.lang.Integer.toString((int) value);
        }
    }

    public static final class Float extends InspectorSlider<java.lang.Float> {
        public Float(
                final Component                         message,
                final FloatProperty                     property,
                final NumberConstraint<java.lang.Float> constraint,
                final float                             step
        ) {
            super(message, property, constraint, step);
        }
    }

}
