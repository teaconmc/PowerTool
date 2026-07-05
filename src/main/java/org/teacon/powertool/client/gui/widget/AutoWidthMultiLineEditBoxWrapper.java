package org.teacon.powertool.client.gui.widget;

import com.xkball.xklib.XKLib;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import com.xkball.xklibmc.x3d.backend.b3d.B3dGuiGraphics;
import dev.vfyjxf.taffy.geometry.FloatSize;
import net.minecraft.client.gui.components.AbstractWidget;
import org.teacon.powertool.annotation.NonNullByDefault;

import java.util.function.Consumer;

@NonNullByDefault
public class AutoWidthMultiLineEditBoxWrapper extends WidgetWrapper {
    
    private final MultiLineEditBox editBox;
    private final int minWidth;
    
    public AutoWidthMultiLineEditBoxWrapper(MultiLineEditBox editBox, int minWidth) {
        super(editBox);
        this.editBox = editBox;
        this.minWidth = minWidth;
        this.setUserInput(true);
    }
    
    public MultiLineEditBox getEditBox() {
        return this.editBox;
    }
    
    @Override
    public AbstractWidget getWidget() {
        return this.editBox;
    }
    
    public void setValue(String value) {
        this.editBox.setValue(value);
        this.markDirty();
    }
    
    public void setValueListener(Consumer<String> valueListener) {
        this.editBox.setValueListener(value -> {
            valueListener.accept(value);
            this.markDirty();
        });
    }
    
    @Override
    public void afterTreeAndNodeSet() {
        super.afterTreeAndNodeSet();
        this.tree.setMeasureFunc(this.nodeId, (knownDimensions, availableSpace) -> {
            float knownWidth = Float.isNaN(knownDimensions.width) ? 0 : knownDimensions.width;
            float width = Math.max(Math.max(knownWidth, this.minWidth), this.getScaledMeasuredWidth());
            float height = Float.isNaN(knownDimensions.height) ? this.getHeight() : knownDimensions.height;
            return new FloatSize(width, height);
        });
    }
    
    @Override
    public void resize(float offsetX, float offsetY) {
        super.resize(offsetX, offsetY);
        var parent = this.getParent();
        if (parent != null) {
            if (this.width > parent.getWidth()) {
                this.setX(offsetX);
            } else {
                this.setX(offsetX + (parent.getWidth() - this.width) / 2);
            }
        }
        this.editBox.setPosition((int) this.x, (int) this.y);
        if (XKLib.RENDER_CONTEXT.get().getGUIGraphics() instanceof B3dGuiGraphics guiGraphics) {
            this.editBox.setSize((int) (this.width / guiGraphics.scale), (int) (this.height / guiGraphics.scale));
        } else {
            this.editBox.setSize((int) this.width, (int) this.height);
        }
    }
    
    private float getScaledMeasuredWidth() {
        if (XKLib.RENDER_CONTEXT.get().getGUIGraphics() instanceof B3dGuiGraphics guiGraphics) {
            return this.editBox.getMeasuredWidth() * guiGraphics.scale;
        }
        return this.editBox.getMeasuredWidth();
    }
}
