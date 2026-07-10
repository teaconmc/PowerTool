package org.teacon.powertool.inspection.property;

import com.xkball.xklibmc.utils.func.FloatSupplier;
import it.unimi.dsi.fastutil.floats.FloatConsumer;

public interface FloatProperty extends NumberProperty<Float> {

    static FloatProperty wrap(
            final FloatConsumer             consumer,
            final float                     value
    ) {
        final var property = new FloatProperty() {

            private float value;

            @Override
            public float getValue() {
                return this.value;
            }

            @Override
            public void setValue(final float value) {
                this.value = value;
                consumer.accept(value);
            }

        };
        property.value = value;
        return property;
    }

    static FloatProperty wrap(
            final FloatConsumer             consumer,
            final FloatSupplier             supplier
    ) {
        return new FloatProperty() {

            @Override
            public float getValue() {
                return supplier.getAsFloat();
            }

            @Override
            public void setValue(final float value) {
                consumer.accept(value);
            }
        };
    }

    static FloatProperty simple(
            final float                     value
    ) {
        final var property = new FloatProperty() {

            private float value;

            @Override
            public float getValue() {
                return this.value;
            }

            @Override
            public void setValue(final float value) {
                this.value = value;
            }
        };
        property.value = value;
        return property;
    }

    float getValue();

    void setValue(final float value);

    @Override
    default Float get() {
        return this.getValue();
    }

    @Override
    default double getNumber() {
        return this.getValue();
    }

    @Override
    default void setNumber(double value) {
        this.setValue((float) value);
    }
}
