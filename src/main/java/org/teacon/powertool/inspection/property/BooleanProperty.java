package org.teacon.powertool.inspection.property;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.inspection.constraint.InputConstraint;

import java.util.function.BooleanSupplier;

public interface BooleanProperty extends Property<Boolean> {

    static BooleanProperty wrap(
            final BooleanConsumer   consumer,
            final boolean           value
    ) {
        return new BooleanProperty() {

            private boolean v = value;

            @Override
            public boolean getValue() {
                return this.v;
            }

            @Override
            public void setValue(final boolean value) {
                this.v = value;
                consumer.accept(value);
            }

        };
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
        return new BooleanProperty() {

            private boolean v = value;

            @Override
            public boolean getValue() {
                return this.v;
            }

            @Override
            public void setValue(final boolean value) {
                this.v = value;
            }
        };
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
