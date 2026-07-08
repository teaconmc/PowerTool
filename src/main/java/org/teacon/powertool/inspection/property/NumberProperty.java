package org.teacon.powertool.inspection.property;

public interface NumberProperty<T extends Number> extends Property<T> {

    void setNumber(float value);

    @Override
    default void set(T value) {
        this.setNumber(value.floatValue());
    }
}
