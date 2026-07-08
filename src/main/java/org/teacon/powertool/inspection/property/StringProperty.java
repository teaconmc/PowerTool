package org.teacon.powertool.inspection.property;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface StringProperty extends Property<String> {

    static StringProperty wrap(
            final Consumer<String> consumer,
            final String value
    ) {
        return new StringProperty() {

            private String v = value;

            @Override
            public String get() {
                return v;
            }

            @Override
            public void set(final String value) {
                this.v = value;
                consumer.accept(value);
            }
        };
    }

    static StringProperty wrap(
            final Consumer<String> consumer,
            final Supplier<String> supplier
    ) {
        return new StringProperty() {

            @Override
            public String get() {
                return supplier.get();
            }

            @Override
            public void set(final String value) {
                consumer.accept(value);
            }
        };
    }

    static StringProperty simple(final String value) {
        return new StringProperty() {

            private String v = value;

            @Override
            public String get() {
                return v;
            }

            @Override
            public void set(final String value) {
                this.v = value;
            }
        };
    }

}
