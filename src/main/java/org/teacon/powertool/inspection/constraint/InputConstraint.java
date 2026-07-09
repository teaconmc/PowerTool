package org.teacon.powertool.inspection.constraint;

import org.jetbrains.annotations.Nullable;

public interface InputConstraint<T> {

    InputConstraint<String> STRING = new InputConstraint<>() {
        @Override
        public boolean isValid(final @Nullable String input) {
            return true;
        }

        @Override
        public String parse(final @Nullable String input) {
            return input;
        }
    };

    boolean isValid(final @Nullable String input);

    T parse(final @Nullable String input);

}
