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
        final var property = new StringProperty() {

            private String value;

            @Override
            public String get() {
                return value;
            }

            @Override
            public void set(final String value) {
                this.value = value;
                consumer.accept(value);
            }
        };
        property.value = value;
        return property;
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
        final var property = new StringProperty() {

            private String value;

            @Override
            public String get() {
                return value;
            }

            @Override
            public void set(final String value) {
                this.value = value;
            }
        };
        property.value = value;
        return property;
    }

}
