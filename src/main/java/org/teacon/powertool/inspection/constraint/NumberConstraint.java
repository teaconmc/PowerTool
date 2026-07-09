package org.teacon.powertool.inspection.constraint;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public interface NumberConstraint<T extends Number> extends InputConstraint<T> {

    // default constraints not allow to drag input
    NumberConstraint<Integer>   INTEGER = integer(Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
    NumberConstraint<Float>     NUMBER  = number(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, 0);

    NumberConstraint<Float>     DEGREES = number(180f, -180f, 0f);

    static NumberConstraint<Integer> integer(
            final int min,
            final int max,
            final int fallback
    ) {
        return new NumberConstraint<>() {

            static final Pattern PATTERN = Pattern.compile("-?\\d+");

            @Override
            public boolean isValid(final @Nullable String input) {
                return input != null && PATTERN.matcher(input).matches();
            }

            @Override
            public Integer parse(final @Nullable String input) {
                if (input == null) {
                    return fallback;
                }

                try {
                    return Integer.parseInt(input);
                } catch (Exception ignored) {
                    return fallback;
                }
            }

            @Override
            public float min() {
                return min;
            }

            @Override
            public float max() {
                return max;
            }
        };
    }

    static NumberConstraint<Float> number(
            final float min,
            final float max,
            final float fallback
    ) {
        return new NumberConstraint<>() {

            static final Pattern PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

            @Override
            public boolean isValid(final @Nullable String input) {
                return input != null && PATTERN.matcher(input).matches();
            }

            @Override
            public Float parse(final @Nullable String input) {
                if (input == null) {
                    return fallback;
                }

                try {
                    return Float.parseFloat(input);
                } catch (Exception ignored) {
                    return fallback;
                }
            }

            @Override
            public float min() {
                return min;
            }

            @Override
            public float max() {
                return max;
            }
        };
    }

    float min();

    float max();

}
