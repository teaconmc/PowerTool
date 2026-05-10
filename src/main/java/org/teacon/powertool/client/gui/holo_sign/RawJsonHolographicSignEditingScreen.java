package org.teacon.powertool.client.gui.holo_sign;

import com.xkball.xklib.ui.widget.ListInputWidget;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import com.xkball.xklibmc.x3d.backend.b3d.gui.ComponentConverter;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;

import java.util.ArrayList;
import java.util.List;

@NonNullByDefault
public class RawJsonHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<RawJsonHolographicSignBlockEntity> {
    
    public List<String> content;
    protected ListInputWidget<Component, ObjectInputWidget<Component>> inputWidget;
    
    public RawJsonHolographicSignEditingScreen(RawJsonHolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit.raw_json"), theSign);
    }
    
    @Override
    protected void readData(RawJsonHolographicSignBlockEntity theSign) {
        super.readData(theSign);
        content = theSign.content;
    }
    
    @Override
    protected Widget createRightPanel() {
        this.inputWidget = new ListInputWidget<>(() -> new ObjectInputWidget<>(ObjectInputBox.COMPONENT_VALIDATOR, ObjectInputBox.COMPONENT_RESPONDER),
                (t, c) -> WidgetWrapper.button(ComponentConverter.toComponent(t),_ -> c.run()));
        for (int i = 0; i < content.size(); i++) {
            this.inputWidget.addNextInput();
            this.inputWidget.getInputWidgets().get(i).setAsString(content.get(i));
        }
        return new ContainerWidget().inlineStyle("""
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        """)
                .addChild(inputWidget.inlineStyle("size: 90% 90%;"));
    }
    
    @Override
    protected void writeBackToBE() {
        super.writeBackToBE();
        sign.content = new ArrayList<>(inputWidget.getInputWidgets().stream().map(ObjectInputWidget::getAsString).toList());
        sign.forFilter = new ArrayList<>(inputWidget.getValue().stream().map(c -> c == null ? Component.empty() : c).toList());
    }
    
}
