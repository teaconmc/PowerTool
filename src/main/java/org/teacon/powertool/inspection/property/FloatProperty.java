package org.teacon.powertool.inspection.property;

public interface FloatProperty extends NumberProperty<Float> {

    float getValue();

    void setValue(final float value);

    @Override
    default Float get() {
        return this.getValue();
    }

    @Override
    default void setNumber(float value) {
        this.setValue(value);
    }
}
