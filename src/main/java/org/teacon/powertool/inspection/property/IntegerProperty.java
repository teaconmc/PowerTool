package org.teacon.powertool.inspection.property;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public interface IntegerProperty extends NumberProperty<Integer> {

    static IntegerProperty wrap(
            final IntConsumer       consumer,
            final int               value
    ) {
        return new IntegerProperty() {

            private int v = value;

            @Override
            public int getValue() {
                return this.v;
            }

            @Override
            public void setValue(final int value) {
                this.v = value;
                consumer.accept(value);
            }

        };
    }

    static IntegerProperty wrap(
            final IntConsumer       consumer,
            final IntSupplier       supplier
    ) {
        return new IntegerProperty() {

            @Override
            public int getValue() {
                return supplier.getAsInt();
            }

            @Override
            public void setValue(final int value) {
                consumer.accept(value);
            }

        };
    }

    static IntegerProperty simple(
            final int               value
    ) {
        return new IntegerProperty() {

            private int v = value;

            @Override
            public int getValue() {
                return this.v;
            }

            @Override
            public void setValue(final int value) {
                this.v = value;
            }
        };
    }

    int getValue();

    void setValue(final int value);

    @Override
    default Integer get() {
        return this.getValue();
    }

    @Override
    default double getNumber() {
        return this.getValue();
    }

    @Override
    default void setNumber(double value) {
        this.setValue((int) value);
    }
}
