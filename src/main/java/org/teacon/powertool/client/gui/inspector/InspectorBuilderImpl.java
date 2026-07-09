package org.teacon.powertool.client.gui.inspector;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.teacon.powertool.inspection.InspectorBuilder;
import org.teacon.powertool.inspection.constraint.InputConstraint;
import org.teacon.powertool.inspection.constraint.NumberConstraint;
import org.teacon.powertool.inspection.property.BooleanProperty;
import org.teacon.powertool.inspection.property.FloatProperty;
import org.teacon.powertool.inspection.property.IntegerProperty;
import org.teacon.powertool.inspection.property.Property;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@SuppressWarnings("unused")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class InspectorBuilderImpl implements InspectorBuilder {

    private final ImmutableList.Builder<InspectorWidget> builder = ImmutableList.builder();

    public List<InspectorWidget> build() {
        return this.builder.build();
    }

    @Override
    public InspectorBuilder space(
            final int               pixels
    ) {
        this.builder.add(new InspectorSpace(pixels));
        return this;
    }

    @Override
    public InspectorBuilder separator(
            final int               top,
            final int               bottom
    ) {
        this.builder.add(new InspectorSeparator(top, bottom));
        return this;
    }


    @Override
    public InspectorBuilder title(
            final Component         component
    ) {
        this.builder.add(new InspectorTitle(component, 16));
        return this;
    }

    @Override
    public InspectorBuilder sliderInt(
            final Component                 component,
            final IntegerProperty           property,
            final NumberConstraint<Integer> constraint,
            final int                       step
    ) {
        this.builder.add(new InspectorSlider.Integer(component, property, step));
        return this;
    }

    @Override
    public InspectorBuilder sliderFloat(
            final Component                 component,
            final FloatProperty             property,
            final NumberConstraint<Float>   constraint,
            final float                     step
    ) {
        this.builder.add(new InspectorSlider.Float(component, property, step));
        return this;
    }

    @Contract("_, _, _ -> this")
    public InspectorBuilder inputString(
            final Component                 component,
            final Property<String>          property,
            final InputConstraint<String>   constraint
    ) {
        this.builder.add(new InspectorEditBox(component, property));
        return this;
    }

    @Contract("_, _, _ -> this")
    public InspectorBuilder inputInt(
            final Component                 component,
            final IntegerProperty           property,
            final NumberConstraint<Integer> constraint
    ) {
        this.builder.add(new InspectorNumberInput<>(
                component,
                property,
                constraint
        ));
        return this;
    }

    @Contract("_, _, _ -> this")
    public InspectorBuilder inputFloat(
            final Component                 component,
            final FloatProperty             property,
            final NumberConstraint<Float>   constraint
    ) {
        this.builder.add(new InspectorNumberInput<>(
                component,
                property,
                constraint
        ));
        return this;
    }

    @Contract("_, _ -> this")
    public InspectorBuilder checkbox(
            final Component         component,
            final BooleanProperty   property
    ) {
        throw new UnsupportedOperationException();
    }
}
