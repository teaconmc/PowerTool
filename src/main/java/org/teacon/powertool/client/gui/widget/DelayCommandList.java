package org.teacon.powertool.client.gui.widget;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.client.gui.SetCommandScreen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DelayCommandList extends EntryListWidget<SetCommandScreen,DelayCommandList.CommandEntry> {
    
    public DelayCommandList(SetCommandScreen screen,int width,int y) {
        super(screen, width,100,y,45);
        this.setX((int) (screen.width*0.3));
        this.resize();
        //this.addEntry(new AppendEntry());
    }
    
    public void resize(){
        this.setHeight(getHeight_());
        this.updateSizeAndPosition(width,height,getY());
        this.setX((int) (screen.width*0.3));
    }
    
    public int getHeight_(){
        return (int) Math.min(Math.max(100,24+45*id),screen.height*0.95-100);
    }
    
    @Override
    void init(SetCommandScreen screen) {
        for(var data : screen.delayedCommands){
            this.addEntry(new CommandEntry(id,data.delay(), data.command()));
            id++;
        }
    }
    
    public void appendEntry(){
        this.appendEntry(new CommandEntry(id,0,""));
    }
    
    @Override
    public void removeEntry(int id){
        super.removeEntry(id);
        screen.refreshContentPos();
    }
    
    @Override
    public int getRowWidth() {
        return width-30;
    }
    
    
    public class CommandEntry extends EntryListWidget.Entry<CommandEntry> {
        public int id;
        public final ObjectInputBox<Integer> delay_;
        public final ObjectInputBox<String> command_;
        protected final Button remove;
        
        public CommandEntry(int id,int delay,String command) {
            this.id = id;
            var box_l = (int)Math.max(100,DelayCommandList.this.screen.width*0.4);
            this.delay_ = new ObjectInputBox<>(Minecraft.getInstance().font,-1,-1,(box_l-20)/2,20,Component.literal("delay"),ObjectInputBox.INT_VALIDATOR,ObjectInputBox.INT_RESPONDER);
            this.delay_.setMaxLength(4);
            this.delay_.setValue(String.valueOf(delay));
            this.command_ = new ObjectInputBox<>(Minecraft.getInstance().font,-1,-1,box_l-20,20,Component.literal("command"),ObjectInputBox.PASS_VALIDATOR,ObjectInputBox.PASS_RESPONDER);
            this.command_.setMaxLength(114514);
            this.command_.setRenderState(false);
            this.command_.setValue(command);
            this.remove = Button.builder(Component.literal("-"),(b) -> DelayCommandList.this.removeEntry(id)).size(20,20).build();
        }
        
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(delay_,command_,remove);
        }
        
        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            var sx = DelayCommandList.this.getX();
            this.delay_.setPosition(sx+20,top);
            this.delay_.render(guiGraphics, mouseX, mouseY, partialTick);
            this.command_.setPosition(sx+20,top+20);
            this.command_.render(guiGraphics, mouseX, mouseY, partialTick);
            this.remove.setPosition(sx+DelayCommandList.this.getWidth()-50,top);
            this.remove.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(delay_,command_,remove);
        }
        
        public int delay(){
            return Objects.requireNonNullElse(this.delay_.get(),0);
        }
        
        public String command(){
            return Objects.requireNonNullElse(this.command_.get(),"");
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
        public CommandEntry copyWithID(int id) {
            return new CommandEntry(id,delay(),command());
        }
    }
}
