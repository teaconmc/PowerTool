package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.client.gui.inspector.InspectorBuilderImpl;
import org.teacon.powertool.client.gui.inspector.InspectorWidget;
import org.teacon.powertool.inspection.Duplicatable;
import org.teacon.powertool.inspection.Inspectable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class Inspector extends EditorWindow {
    private static final int        HEAD_HEIGHT = 24;  // dummy

    private List<InspectorWidget>   widgets;
    private Inspectable             target;

    private InspectorWidget         selected;

    public Inspector(int width, int height) {
        super(0, 0, width, height, Component.literal("Inspector"));
    }

    public boolean isInspecting(Inspectable object) {
        return this.target == object;
    }

    public void setInspectObject(Inspectable target) {
        if (target != this.target) {
            this.target = target;
            this.rebuildInspector();
        }
    }

    public void rebuildInspector() {
        if (this.target == null) {
            this.widgets = null;
            return;
        }

        InspectorBuilderImpl builder = new InspectorBuilderImpl();
        target.onInspect(builder);
        this.widgets = builder.build();

        int height = 0;
        for (InspectorWidget widget : this.widgets) {
            height += widget.getHeight();
            widget.resize(this.getFrameWidth());
        }
        this.updateScrollHeight(height);
    }

    @Override
    protected boolean skipRender() {
        return this.widgets == null || this.widgets.isEmpty();
    }


    @Override
    protected void extractInnerRenderState(
            final GuiGraphicsExtractor  graphics,
            int                         localMouseX,
            int                         localMouseY,
            float                       partialTick
    ) {

        final var pose = graphics.pose();

        int offset = (int) this.getOffset();
        pose.translate(0, -offset);

        int height = 0;

        int mouseX = localMouseX;
        int mouseY = localMouseY + offset;

        for (InspectorWidget widget : this.widgets) {

            final var hovered = this.isHovered && mouseY > height && mouseY < height + widget.getHeight();

            widget.render(
                    graphics,
                    mouseX,
                    mouseY - height,
                    partialTick,
                    hovered
            );

            pose.translate(0f, widget.getHeight());
            height += widget.getHeight();
        }
    }

    @Override
    protected Duplicatable getSelected() {
        return this.selected;
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {

        if (this.widgets == null) {
            return false;
        }

        var remapped    = this.remap(event);
        var mouseX      = remapped.x();
        var mouseY      = remapped.y();

        int height = 0;
        for (InspectorWidget widget : this.widgets) {
            if (mouseY > height && mouseY < height + widget.getHeight()) {
                widget.onMousePressed(
                        new MouseButtonEvent(
                                mouseX,
                                mouseY - height,
                                remapped.buttonInfo()
                        ),
                        doubleClick
                );

                if (this.selected != null && this.selected != widget) {
                    this.selected.setFocused(false);
                }

                this.selected = widget;
                this.selected.setFocused(true);
                return true;
            }

            height += widget.getHeight();
        }

        if (this.selected != null) {
            this.selected.setFocused(false);
        }
        this.selected = null;
        return false;
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {

        if (this.widgets == null) {
            return false;
        }

        var remapped    = this.remap(event);
        var mouseX      = remapped.x();
        var mouseY      = remapped.y();

        int height = 0;
        for (InspectorWidget widget : this.widgets) {
            if (mouseY > height && mouseY < height + widget.getHeight()) {
                widget.onMouseReleased(
                        new MouseButtonEvent(
                                mouseX,
                                mouseY - height,
                                event.buttonInfo()
                        )
                );
                return true;
            }
            height += widget.getHeight();
        }
        return false;
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {

        if (this.widgets == null) {
            return false;
        }

        var remapped    = this.remap(event);
        var mouseX      = remapped.x();
        var mouseY      = remapped.y();

        int height = 0;
        for (InspectorWidget widget : this.widgets) {
            if (mouseY > height && mouseY < height + widget.getHeight()) {
                widget.onMouseDragging(new MouseButtonEvent(
                        mouseX,
                        mouseY - height,
                        event.buttonInfo()
                ), dx, dy);
                return true;
            }
            height += widget.getHeight();
        }
        return false;
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (this.selected != null && this.selected.onKeyPressed(event)) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(final KeyEvent event) {
        if (this.selected != null) {
            return this.selected.onKeyReleased(event);
        }
        return false;
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        if (this.selected != null) {
            return this.selected.charTyped(event);
        }
        return false;
    }

    @Override
    protected void resizeFrame(
            final int innerWidth,
            final int innerHeight
    ) {
        if (this.widgets != null) {
            for (final var widget : this.widgets) {
                widget.resize(innerWidth);
            }
        }
    }
}
