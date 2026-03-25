package org.teacon.powertool.client.gui.widget;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.client.gui.holo_sign.RawJsonHolographicSignEditingScreen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JsonComponentList extends EntryListWidget<RawJsonHolographicSignEditingScreen,JsonComponentList.Entry> {
    
    public JsonComponentList(RawJsonHolographicSignEditingScreen screen,int width, int y) {
        super(screen, width, (int) (screen.height*0.6), y, 30);
        this.setX((int) (200+screen.width*0.05));
    }
    
    @Override
    void init(RawJsonHolographicSignEditingScreen screen) {
        for(var data: screen.content){
            this.appendEntry(new Entry(id,data));
        }
        if(id == 0) this.appendEntry(new Entry(id,""));
    }
    
    public void appendEntry() {
        this.appendEntry(new Entry(id,""));
    }
    
    @Override
    public int getRowWidth() {
        return width-30;
    }
    
    public class Entry extends EntryListWidget.Entry<Entry> {
        public int id;
        public final ObjectInputBox<Component> content;
        protected final Button remove;
        
        public Entry(int id,String content) {
            this.id = id;
            var box_l = Math.max(100,JsonComponentList.this.width);
            this.content = new ObjectInputBox<>(Minecraft.getInstance().font,-1,-1,box_l-80,20,Component.literal("component"),ObjectInputBox.COMPONENT_VALIDATOR,ObjectInputBox.COMPONENT_RESPONDER);
            this.content.setMaxLength(114514);
            this.content.setValue(content);
            this.remove = Button.builder(Component.literal("-"),(b) -> JsonComponentList.this.removeEntry(id))
                    .size(20,20)
                    .build();
            remove.active = id != 0;
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(content,remove);
        }
        
        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            var sx = JsonComponentList.this.getX();
            this.content.setPosition(sx+20,top);
            this.content.render(guiGraphics, mouseX, mouseY, partialTick);
            this.remove.setPosition(sx+JsonComponentList.this.getWidth()-50,top);
            this.remove.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(content,remove);
        }
        
        @Override
        public void setID(int id) {
            this.id = id;
            remove.active = id != 0;
        }
        
        @Override
        public int getID() {
            return id;
        }
        
        public String contentString(){
            return this.content.getValue();
        }
        
        public Component content(){
            return Objects.requireNonNullElse(this.content.get(),Component.empty());
        }
        
        @Override
        public Entry copyWithID(int id) {
            return new Entry(id,contentString());
        }
    }
}
