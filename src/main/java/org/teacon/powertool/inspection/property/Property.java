package org.teacon.powertool.inspection.property;

public interface Property<T> {

    T get();

    void set(T value);

}
