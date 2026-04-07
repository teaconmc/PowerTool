package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public abstract class EntryListWidget<S extends Screen, E extends EntryListWidget.Entry<E>> extends ContainerObjectSelectionList<E> {
    
    protected int id = 0;
    protected S screen;
    
    public EntryListWidget(S screen, int width, int height, int y, int itemHeight) {
        super(Minecraft.getInstance(), width, height, y, itemHeight);
        this.screen = screen;
//        this.setRenderHeader(true, 24);
        this.init(screen);
    }
    
    abstract void init(S screen);
    
    public List<E> entries() {
        return children();
    }
    
    public void appendEntry(E entry) {
        entry.setID(id);
        this.addEntry(entry);
        id++;
    }
    
    public void removeEntry(int id) {
        var newEntries = new ArrayList<E>();
        int nid = 0;
        for (var entry : children()) {
            if (entry.getID() != id) {
                newEntries.add(entry.copyWithID(nid));
                nid++;
            }
        }
        this.replaceEntries(newEntries);
        this.id = newEntries.size();
    }
    
    @Override
    public void clearEntries() {
        super.clearEntries();
        id = 0;
    }
    
    public static abstract class Entry<E extends Entry<E>> extends ContainerObjectSelectionList.Entry<E> {
        public abstract void setID(int id);
        
        public abstract int getID();
        
        public abstract E copyWithID(int id);
    }
}
