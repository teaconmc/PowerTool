package org.teacon.powertool.inspection.property;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.BooleanSupplier;

public interface BooleanProperty extends Property<Boolean> {

    static BooleanProperty wrap(
            final BooleanConsumer   consumer,
            final boolean           value
    ) {
        final var property = new BooleanProperty() {

            private boolean value;

            @Override
            public boolean getValue() {
                return this.value;
            }

            @Override
            public void setValue(final boolean value) {
                this.value = value;
                consumer.accept(value);
            }

        };
        property.value = value;
        return property;
    }

    static BooleanProperty wrap(
            final BooleanConsumer   consumer,
            final BooleanSupplier   supplier
    ) {
        return new BooleanProperty() {

            @Override
            public boolean getValue() {
                return supplier.getAsBoolean();
            }

            @Override
            public void setValue(final boolean value) {
                consumer.accept(value);
            }

        };
    }

    static BooleanProperty simple(
            final boolean           value
    ) {
        final var property = new BooleanProperty() {

            private boolean value;

            @Override
            public boolean getValue() {
                return this.value;
            }

            @Override
            public void setValue(final boolean value) {
                this.value = value;
            }
        };
        property.value = value;
        return property;
    }

    boolean getValue();

    void setValue(final boolean value);

    @Override
    default Boolean get() {
        return this.getValue();
    }

    @Override
    default void set(final Boolean value) {
        this.setValue(value);
    }
}
