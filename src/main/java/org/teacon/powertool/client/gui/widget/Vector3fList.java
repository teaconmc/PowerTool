package org.teacon.powertool.client.gui.widget;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.joml.Vector3f;
import org.teacon.powertool.client.gui.BezierCurveBlockScreen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Vector3fList extends EntryListWidget<BezierCurveBlockScreen, Vector3fList.Entry> {
    
    public Vector3fList(BezierCurveBlockScreen screen, int height, int y) {
        super(screen, (int) (screen.width * 0.4), height, y, 30);
        this.setX((int) (screen.width * 0.55));
    }
    
    @Override
    void init(BezierCurveBlockScreen screen) {
        for (var point : screen.te.controlPoints) {
            this.appendEntry(new Entry(id, point));
        }
    }
    
    public void appendEntry() {
        this.appendEntry(new Entry(id, new Vector3f()));
    }
    
    @Override
    public int getRowWidth() {
        return width - 4;
    }
    
    public class Entry extends EntryListWidget.Entry<Entry> {
        public int id;
        protected final ObjectInputBox<Float> x;
        protected final ObjectInputBox<Float> y;
        protected final ObjectInputBox<Float> z;
        protected final Button remove;
        
        public Entry(int id, Vector3f content) {
            this.id = id;
            var box_l = (int) (Vector3fList.this.screen.width * 0.08);
            this.x = new ObjectInputBox<>(Minecraft.getInstance().font, -1, -1, box_l, 20, Component.literal("X: "), ObjectInputBox.FLOAT_VALIDATOR, ObjectInputBox.FLOAT_RESPONDER);
            this.x.setMaxLength(20);
            this.x.setValue(String.valueOf(content.x));
            this.y = new ObjectInputBox<>(Minecraft.getInstance().font, -1, -1, box_l, 20, Component.literal("Y: "), ObjectInputBox.FLOAT_VALIDATOR, ObjectInputBox.FLOAT_RESPONDER);
            this.y.setMaxLength(20);
            this.y.setValue(String.valueOf(content.y));
            this.z = new ObjectInputBox<>(Minecraft.getInstance().font, -1, -1, box_l, 20, Component.literal("Z: "), ObjectInputBox.FLOAT_VALIDATOR, ObjectInputBox.FLOAT_RESPONDER);
            this.z.setMaxLength(20);
            this.z.setValue(String.valueOf(content.z));
            this.remove = Button.builder(Component.literal("-"), (b) -> Vector3fList.this.removeEntry(id))
                    .size(20, 20)
                    .build();
        }
        
        @Override
        public void setID(int id) {
            this.id = id;
        }
        
        @Override
        public int getID() {
            return id;
        }
        
        @Override
        public Entry copyWithID(int id) {
            return new Entry(id, getResult());
        }
        
        public Vector3f getResult() {
            var x = Objects.requireNonNullElse(this.x.get(), 0f);
            var y = Objects.requireNonNullElse(this.y.get(), 0f);
            var z = Objects.requireNonNullElse(this.z.get(), 0f);
            return new Vector3f(x, y, z);
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(x, y, z, remove);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(x, y, z, remove);
        }
        
        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            var x = Vector3fList.this.getX();
            var top = this.getY();
            width = Vector3fList.this.screen.width;
            var box_l = (int) (Vector3fList.this.screen.width * 0.08);
            width = (int) (width * 0.4);
            this.x.setPosition(x + 30, top);
            this.y.setPosition(x + (width - box_l) / 2 - 12, top);
            this.z.setPosition(x + width - 50 - box_l - 5, top);
            this.remove.setPosition(x + width - 50, top);
            this.x.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            this.y.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            this.z.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            this.remove.extractRenderState(graphics, mouseX, mouseY, a);
        }
    }
}
