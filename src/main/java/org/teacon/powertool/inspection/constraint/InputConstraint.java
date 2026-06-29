package org.teacon.powertool.inspection.constraint;

import org.jetbrains.annotations.Nullable;

public interface InputConstraint {

    boolean isValid(final @Nullable String input);

}
