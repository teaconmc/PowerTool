package org.teacon.powertool.inspection;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.teacon.powertool.inspection.property.BooleanProperty;
import org.teacon.powertool.inspection.property.FloatProperty;
import org.teacon.powertool.inspection.property.IntegerProperty;
import org.teacon.powertool.inspection.property.Property;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface InspectorBuilder {

    @Contract(" -> this")
    default InspectorBuilder space() {
        return this.space(10);
    }

    @Contract("_ -> this")
    InspectorBuilder space(
            final int               pixels
    );

    @Contract("_ -> this")
    InspectorBuilder title(
            final Component         component
    );

    @Contract("_, _, _ -> this")
    InspectorBuilder sliderInt(
            final Component         component,
            final IntegerProperty   property,
            final int               step
    );

    @Contract("_, _, _ -> this")
    InspectorBuilder sliderFloat(
            final Component         component,
            final FloatProperty     property,
            final float             step
    );

    @Contract("_, _ -> this")
    InspectorBuilder inputString(
            final Component         component,
            final Property<String>  property
    );

    @Contract("_, _ -> this")
    InspectorBuilder inputInt(
            final Component         component,
            final IntegerProperty   property
    );

    @Contract("_, _ -> this")
    InspectorBuilder inputFloat(
            final Component         component,
            final FloatProperty     property
    );

    @Contract("_, _ -> this")
    InspectorBuilder checkbox(
            final Component         component,
            final BooleanProperty   property
    );

}
