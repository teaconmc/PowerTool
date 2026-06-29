package org.teacon.powertool.inspection.property;

public interface IntegerProperty extends NumberProperty<Integer> {

    int getValue();

    void setValue(final int value);

    @Override
    default Integer get() {
        return this.getValue();
    }

    @Override
    default void setNumber(float value) {
        this.setValue((int) value);
    }
}
