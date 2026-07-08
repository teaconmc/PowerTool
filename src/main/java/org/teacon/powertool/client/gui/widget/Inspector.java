package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.client.gui.inspector.InspectorBuilderImpl;
import org.teacon.powertool.client.gui.inspector.InspectorWidget;
import org.teacon.powertool.inspection.Inspectable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class Inspector extends EditorWindow {
    private static final int        HEAD_HEIGHT = 24;  // dummy

    private List<InspectorWidget>   widgets;
    private Inspectable             target;

    private InspectorWidget         focus;
    private InspectorWidget         copy;

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
            int                         innerWidth,
            int                         innerHeight,
            float                       partialTick
    ) {

        final var pose = graphics.pose();

        int offset = (int) this.getOffset();
        pose.translate(0, -offset);

        int maxHeight = this.height + offset;
        int height = 0;

        localMouseX -= this.getX() + 8;
        localMouseY -= this.getY() - offset;

        for (InspectorWidget widget : this.widgets) {

            if (height > offset && height < maxHeight) {
                widget.render(
                        graphics,
                        innerWidth,
                        localMouseX,
                        localMouseY - height,
                        partialTick,
                        this.isHovered && localMouseY > height && localMouseY < height + widget.getHeight()
                );
            }

            pose.translate(0f, widget.getHeight());
            height += widget.getHeight();
        }
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
                        doubleClick, this.width
                );

                if (this.focus != null && this.focus != widget) {
                    this.focus.setFocused(false);
                }

                this.focus = widget;
                this.focus.setFocused(true);
                return true;
            }

            height += widget.getHeight();
        }

        if (this.focus != null) {
            this.focus.setFocused(false);
        }
        this.focus = null;
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
                        ), this.width
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
                ), dx, dy, this.width);
                return true;
            }
            height += widget.getHeight();
        }
        return false;
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (this.focus != null) {
            return this.focus.onKeyPressed(event);
        }
        return false;
    }

    @Override
    public boolean keyReleased(final KeyEvent event) {
        if (this.focus != null) {
            if (this.focus.onKeyReleased(event)) {
                return true;
            }

            if (event.isCopy()) {
                this.copy = this.focus;
                return true;
            } else if (this.copy != null && event.isPaste()) {
                this.focus.paste(this.copy);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        if (this.focus != null) {
            return this.focus.charTyped(event);
        }
        return false;
    }
}
