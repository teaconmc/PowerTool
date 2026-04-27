/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.client.gui.holo_sign;

import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import com.xkball.xklibmc.ui.widget.mc.XKLibMultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;

import java.util.Arrays;

@NonNullByDefault
public class CommonHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<CommonHolographicSignBlockEntity> {
    
    public static final int MAXIMUM_LINE_COUNT = 10;
    
    private String[] messages;
    
    public CommonHolographicSignEditingScreen(CommonHolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit"), theSign);
    }
    
    @Override
    protected void readData(CommonHolographicSignBlockEntity theSign) {
        super.readData(theSign);
        var size = theSign.contents.size();
        this.messages = new String[Math.max(size, MAXIMUM_LINE_COUNT)];
        Arrays.fill(messages, "");
        for (int i = 0; i < size; i++) {
            messages[i] = theSign.contents.get(i).getString();
        }
    }
    
    @Override
    protected Widget createRightPanel() {
        var textWidget = WidgetWrapper.multiLineTextWidget();
        var multiLineWidget = ((XKLibMultiLineEditBox) textWidget.getWidget());
        multiLineWidget.setValue(String.join("\n", messages));
        multiLineWidget.setValueListener(s -> {
            var array = s.split("\n");
            if(array.length > MAXIMUM_LINE_COUNT){
                this.messages = Arrays.copyOf(array, MAXIMUM_LINE_COUNT);
                multiLineWidget.setValue(String.join("\n", messages));
            }
            else {
                this.messages = array;
            }
        });
        return new ContainerWidget().inlineStyle("""
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        """)
                .addChild(textWidget.inlineStyle("size: 60% 60%"));
    }
    
    @Override
    protected void writeBackToBE() {
        super.writeBackToBE();
        var toSend = Arrays.copyOfRange(this.messages, 0, this.messages.length);
        this.sign.contents = Arrays.stream(toSend).map(Component::literal).limit(MAXIMUM_LINE_COUNT).toList();
    }
    
}
