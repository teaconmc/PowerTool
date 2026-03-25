package org.teacon.powertool.client.gui.widget;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.teacon.powertool.client.gui.ExamineHoloGlassScreen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockEntityList extends EntryListWidget<ExamineHoloGlassScreen, BlockEntityList.Entry> {
    
    
    public BlockEntityList(ExamineHoloGlassScreen screen, int width, int height, int y, int itemHeight) {
        super(screen, width, height, y, itemHeight);
        this.setX((int) (screen.width*0.55));
    }
    
    @Override
    void init(ExamineHoloGlassScreen screen) {
        update();
    }
    
    public void update(){
        this.clearEntries();
        var blockData = screen.blocksData;
        for(var entry : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()){
            var key = entry.getKey();
            var value = entry.getValue();
            var selected = value.getValidBlocks().stream().map(BuiltInRegistries.BLOCK::getKey).anyMatch(blockData::contains);
            addEntry(new Entry(id,key.identifier().toString(),value,selected));
        }
    }
    
    @Override
    public int getRowWidth() {
        return width-30;
    }
    
    public Set<Identifier> getResult(){
        return entries().stream()
                .flatMap(entry -> entry.getResult().stream())
                .map(BuiltInRegistries.BLOCK::getKey)
                .collect(Collectors.toSet());
    }
    
    public static class Entry extends EntryListWidget.Entry<Entry> {
        public int id;
        protected final String name;
        protected final BlockEntityType<?> type;
        protected final Checkbox checkbox;
        
        public Entry(int id,String name, BlockEntityType<?> blockEntityType,boolean selected) {
            this.id = id;
            this.name = name;
            this.type = blockEntityType;
            this.checkbox = Checkbox.builder(Component.literal(name), Minecraft.getInstance().font)
                    .selected(selected)
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
            return new Entry(id, name,type,checkbox.selected());
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(checkbox);
        }
        
        public Set<Block> getResult(){
            if(!checkbox.selected()) return Set.of();
            return type.getValidBlocks();
        }
        
        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            checkbox.setPosition(left,top);
            checkbox.extractContents(graphics, mouseX, mouseY, a);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(checkbox);
        }
    }
}
