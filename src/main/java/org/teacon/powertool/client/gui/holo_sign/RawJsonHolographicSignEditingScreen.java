package org.teacon.powertool.client.gui.holo_sign;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.client.gui.widget.JsonComponentList;

import java.util.ArrayList;
import java.util.List;

public class RawJsonHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<RawJsonHolographicSignBlockEntity> {
    
    public List<String> content;
    protected Button append;
    protected JsonComponentList jsonComponentList;
    
    public RawJsonHolographicSignEditingScreen(RawJsonHolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit.raw_json"), theSign);
        content = theSign.content;
    }
    
    @Override
    protected void init() {
        super.init();
        
        var startY = (int) (height * 0.05);
        this.append = Button.builder(Component.literal("+"), (b) -> {
            if (this.jsonComponentList != null) jsonComponentList.appendEntry();
        }).size(20, 20).pos(width - 45, startY + 52).build();
        this.jsonComponentList = new JsonComponentList(this, (int) (width - 220 - width * 0.05), startY + 50);
        this.addRenderableWidget(jsonComponentList);
        this.addRenderableWidget(append);
    }
    
    @Override
    public int getDoneButtonY() {
        var startY = (int) (height * 0.15);
        return (int) (startY + height * 0.7 + 15);
    }
    
    @Override
    protected void writeBackToBE() {
        super.writeBackToBE();
        sign.content = new ArrayList<>(jsonComponentList.entries().stream().map(JsonComponentList.Entry::contentString).toList());
        sign.forFilter = new ArrayList<>(jsonComponentList.entries().stream().map(JsonComponentList.Entry::content).toList());
    }
    
}
