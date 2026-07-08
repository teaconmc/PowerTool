package org.teacon.powertool.inspection.constraint;

public interface NumberConstraint<T extends Number> extends InputConstraint<T> {

    float min();

    float max();

}
