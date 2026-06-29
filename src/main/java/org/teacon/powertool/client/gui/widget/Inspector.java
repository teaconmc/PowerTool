package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.teacon.powertool.client.gui.inspector.InspectorBuilderImpl;
import org.teacon.powertool.client.gui.inspector.InspectorWidget;
import org.teacon.powertool.inspection.Inspectable;

import java.util.List;

public class Inspector extends AbstractWidget {
    private static final int        HEAD_HEIGHT = 24;  // dummy

    private List<InspectorWidget>   widgets;
    private Inspectable             target;

    private InspectorWidget         focus;
    private InspectorWidget         copy;

    private int                     frameHeight;
    private int                     scroll = 0;
    private int                     available = 0;

    private float                   ratio = 1.0f;

    public Inspector(int width, int height) {
        super(0, 0, width, height, Component.literal("Inspector"));
        this.frameHeight = height - HEAD_HEIGHT - 8;
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
    }

    @Override
    protected void extractWidgetRenderState(
            final GuiGraphicsExtractor  graphics,
            int                   mouseX,
            int                   mouseY,
            float                 partialTick
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x88000000);
        graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xffffffff);

        int top = this.getY() + HEAD_HEIGHT;

        graphics.horizontalLine(this.getX() + 8, this.getX() + this.getWidth() - 8, top, 0xffffffff);
        graphics.centeredText(minecraft.font, "Inspector", this.getX() + this.width / 2, 8, 0xffffffff);

        if (this.widgets == null) {
            return;
        }

        boolean hovered = this.isMouseOver(mouseX, mouseY);

        int innerLeft = this.getX() + 8;
        int innerWidth = this.getWidth() - 16;

        var pose = graphics.pose();
        pose.pushMatrix();
        int offset = (int) (this.scroll / this.ratio);
        pose.translate(innerLeft, top + 1);

        graphics.enableScissor(
                0, 0,
                width, this.frameHeight
        );

        pose.translate(0, -offset);

        int maxHeight = this.height + offset;
        int height = HEAD_HEIGHT;
        int width = this.width - 2;

        mouseX -= this.getX() + 8;
        mouseY -= this.getY() - offset;

        for (InspectorWidget widget : this.widgets) {

            if (height > offset && height < maxHeight) {
                widget.render(
                        graphics,
                        innerWidth,
                        mouseX,
                        mouseY - height,
                        partialTick,
                        hovered && mouseY > height && mouseY < height + widget.getHeight()
                );
            }

            pose.translate(0f, widget.getHeight());
            height += widget.getHeight();
        }

        graphics.disableScissor();
        pose.popMatrix();


        int scrollHeight = height;
        int channelHeight = this.frameHeight - 4;
        this.ratio = Math.min((float) this.frameHeight / scrollHeight, 1.0f);
        int sliderHeight = (int) (ratio * channelHeight);

        int sliderTop = top + 4;
        int left = this.getX() + width - 4;

        this.available = channelHeight - sliderHeight;
        graphics.fill(left - 1, sliderTop, left + 3, sliderTop + 1, 0xffffffff);
        graphics.fill(
                left,
                sliderTop + 2 + Math.max(this.scroll, 0),
                left + 2,
                sliderTop + 2 + sliderHeight + Math.min(this.scroll, this.available),
                0xffffffff
        );
        graphics.fill(left - 1, sliderTop + 3 + channelHeight, left + 3, sliderTop + 4 + channelHeight, 0xffffffff);
        this.scroll = Math.clamp(this.scroll, 0, this.available);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {

        var mouseX      = event.x();
        var mouseY      = event.y();

        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        mouseX -= this.getX() + 8;
        mouseY -= this.getY() - this.scroll / this.ratio;

        var remapped    = new MouseButtonEvent(mouseX, mouseY, event.buttonInfo());

        int height = HEAD_HEIGHT;
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

        var mouseX = event.x();
        var mouseY = event.y();

        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        mouseX -= this.getX() + 8;
        mouseY -= this.getY() - this.scroll / this.ratio;

        int height = HEAD_HEIGHT;
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

        var mouseX = event.x();
        var mouseY = event.y();

        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        mouseX -= this.getX() + 8;
        mouseY -= this.getY() - this.scroll / this.ratio;

        int height = HEAD_HEIGHT;
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
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (!this.isMouseOver(pMouseX, pMouseY)) {
            return false;
        }

        this.scroll -= (int) pScrollY * 4;
        this.scroll = Math.clamp(this.scroll, 0, this.available);

        return true;
    }

    @Override
    public boolean keyPressed(final @NonNull KeyEvent event) {
        if (this.focus != null) {
            return this.focus.onKeyPressed(event);
        }
        return false;
    }

    @Override
    public boolean keyReleased(final @NonNull KeyEvent event) {
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
    public boolean charTyped(final @NonNull CharacterEvent event) {
        if (this.focus != null) {
            return this.focus.charTyped(event);
        }
        return false;
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        this.frameHeight = height - HEAD_HEIGHT - 8;
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.frameHeight = height - HEAD_HEIGHT - 8;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}
