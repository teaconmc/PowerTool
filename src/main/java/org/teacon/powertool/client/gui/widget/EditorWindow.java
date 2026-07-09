package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class EditorWindow extends AbstractWidget {

    private static final int        HEAD_HEIGHT = 24;  // dummy

    private int                     frameHeight;
    private int                     channelHeight;
    private int                     sliderHeight;
    private int                     scroll = 0;
    private int                     available = 0;

    private float                   ratio = 1.0f;

    public EditorWindow(final int x, final int y, final int width, final int height, final Component message) {
        super(x, y, width, height, message);
        this.updateFrameHeight();
        this.updateScrollHeight(0);
    }

    protected abstract boolean skipRender();

    protected abstract void extractInnerRenderState(
            final GuiGraphicsExtractor  graphics,
            int                         localMouseX,
            int                         localMouseY,
            int                         innerWidth,
            int                         innerHeight,
            float                       partialTick
    );

    @Override
    protected void extractWidgetRenderState(
            final GuiGraphicsExtractor graphics,
            final int mouseX,
            final int mouseY,
            final float partialTick
    ) {

        Minecraft minecraft = Minecraft.getInstance();

        graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x88000000);
        graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xffffffff);

        int top = this.getY() + HEAD_HEIGHT;

        graphics.horizontalLine(this.getX() + 8, this.getX() + this.getWidth() - 8, top, 0xffffffff);
        graphics.centeredText(minecraft.font, this.getMessage(), this.getX() + this.width / 2, 8, 0xffffffff);

        if (this.skipRender()) {
            return;
        }

        int innerTop = top + 4;
        int innerLeft = this.getX() + 8;
        int innerWidth = this.getWidth() - 16;
        int innerHeight = this.frameHeight;

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(innerLeft, innerTop);

        graphics.enableScissor(
                0, 0,
                innerWidth, innerHeight
        );

        this.isHovered = this.isMouseOver(mouseX, mouseY);

        this.extractInnerRenderState(
                graphics,
                mouseX - innerLeft,
                mouseY - innerTop,
                innerWidth,
                innerHeight,
                partialTick
        );

        graphics.disableScissor();
        pose.popMatrix();

        int channelHeight = this.channelHeight;
        int sliderHeight = this.sliderHeight;

        int sliderTop = top + 4;
        int left = this.getX() + width - 4;

        graphics.fill(left - 1, sliderTop, left + 3, sliderTop + 1, 0xffffffff);
        graphics.fill(
                left,
                sliderTop + 2 + Math.max(this.scroll, 0),
                left + 2,
                sliderTop + 2 + sliderHeight + Math.min(this.scroll, this.available),
                0xffffffff
        );
        graphics.fill(left - 1, sliderTop + 3 + channelHeight, left + 3, sliderTop + 4 + channelHeight, 0xffffffff);

    }


    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (!this.isHovered) {
            return false;
        }

        this.scroll -= (int) pScrollY * 4;
        this.scroll = Math.clamp(this.scroll, 0, this.available);

        return true;
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        this.updateFrameHeight();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.updateFrameHeight();
    }

    protected float getOffset() {
        return this.scroll * this.ratio;
    }

    protected MouseButtonEvent remap(final MouseButtonEvent event) {
        return new MouseButtonEvent(
                event.x() - this.getX() - 8,
                event.y() - this.getY() - HEAD_HEIGHT - 4 + this.getOffset(),
                event.buttonInfo()
        );
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput output) {

    }

    private void updateFrameHeight() {
        this.frameHeight = height - HEAD_HEIGHT - 8;
        this.channelHeight = this.frameHeight - 4;
    }

    protected void updateScrollHeight(int scrollHeight) {
        float sh = Math.max(scrollHeight + HEAD_HEIGHT, 1e-8f);
        float fh = Math.max(this.frameHeight, 1e-8f);

        this.ratio = Math.min(sh / fh, 1.0f);

        this.sliderHeight = Math.min((int) ((fh / sh) * this.channelHeight), this.channelHeight);
        this.available = Math.max(this.channelHeight - this.sliderHeight, 0);
        this.scroll = Math.clamp(this.scroll, 0, this.available);
    }
}
