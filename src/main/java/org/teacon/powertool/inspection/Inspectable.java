package org.teacon.powertool.inspection;

import org.jspecify.annotations.NonNull;

public interface Inspectable {

    void onInspect(final @NonNull InspectorBuilder builder);

}
