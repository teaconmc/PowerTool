package org.teacon.powertool.client.gui.inspector;

import net.minecraft.network.chat.Component;
import org.teacon.powertool.inspection.property.Property;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class InspectorModificationWidget<T> extends InspectorWidget {
    protected final Component   message;
    protected final Property<T> property;

    protected InspectorModificationWidget(
            int         height,
            Component   message,
            Property<T> property
    ) {
        super(height);
        this.message    = message;
        this.property   = property;
    }
}
