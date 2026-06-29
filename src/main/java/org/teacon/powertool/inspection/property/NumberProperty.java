package org.teacon.powertool.inspection.property;

import org.teacon.powertool.inspection.constraint.NumberConstraint;

public interface NumberProperty<T extends Number> extends Property<T> {

    @Override
    NumberConstraint getConstraint();

    void setNumber(float value);

    @Override
    default void set(T value) {
        this.setNumber(value.floatValue());
    }
}
