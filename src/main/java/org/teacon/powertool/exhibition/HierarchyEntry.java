package org.teacon.powertool.exhibition;

import org.teacon.powertool.inspection.Duplicatable;

import java.util.Collection;

public interface HierarchyEntry extends Duplicatable {
    String name();

    Collection<HierarchyEntry> children();

    default int color() {
        return 0xff888888;
    }
}
