package org.teacon.powertool.inspection.property;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Vector3fProperty implements Property<Vector3fc> {

    public final FloatProperty x;
    public final FloatProperty y;
    public final FloatProperty z;

    private final Vector3f value;

    public static Vector3fProperty of(
            final Vector3fc value
    ) {
        return new Vector3fProperty(value.x(), value.y(), value.z());
    }

    public static Vector3fProperty of(
            final float px,
            final float py,
            final float pz
    ) {
        return new Vector3fProperty(px, py, pz);
    }

    private Vector3fProperty(
            float px,
            float py,
            float pz
    ) {
        this.value = new Vector3f(px, py, pz);

        this.x = FloatProperty.wrap(
                (x) -> this.value.x = x,
                this.value::x
        );
        this.y = FloatProperty.wrap(
                (y) -> this.value.y = y,
                this.value::y
        );
        this.z = FloatProperty.wrap(
                (z) -> this.value.z = z,
                this.value::z
        );
    }

    @Override
    public Vector3fc get() {
        return this.value;
    }

    @Override
    public void set(final Vector3fc value) {
        this.value.set(value);
        this.x.set(value.x());
        this.y.set(value.y());
        this.z.set(value.z());
    }
}
