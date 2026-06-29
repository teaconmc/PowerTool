package org.teacon.powertool.inspection.property;

import org.teacon.powertool.inspection.constraint.InputConstraint;

public interface Property<T> {

    InputConstraint getConstraint();

    T get();

    void set(T value);

}
