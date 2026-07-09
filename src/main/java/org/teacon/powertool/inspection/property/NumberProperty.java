package org.teacon.powertool.inspection.property;

public interface NumberProperty<T extends Number> extends Property<T> {

    void setNumber(double value);

    double getNumber();

    @Override
    default void set(T value) {
        this.setNumber(value.floatValue());
    }
}
