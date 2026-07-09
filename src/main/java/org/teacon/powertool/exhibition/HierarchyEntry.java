package org.teacon.powertool.exhibition;

import java.util.Collection;

public interface HierarchyEntry {
    String name();

    Collection<HierarchyEntry> children();
}
