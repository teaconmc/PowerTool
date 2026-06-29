package org.teacon.powertool.inspection.property;

public interface BooleanProperty extends Property<Boolean> {

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
