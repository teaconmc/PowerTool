package org.teacon.powertool.client.gui.holo_sign;

import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;

import java.util.function.Consumer;
import java.util.function.Supplier;

@NonNullByDefault
public class LinkHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<LinkHolographicSignBlockEntity> {
    
    String display;
    String url;
    
    public LinkHolographicSignEditingScreen(LinkHolographicSignBlockEntity theSign) {
        display = theSign.displayContent.getString();
        url = theSign.url;
        super(Component.translatable("sign.edit.link"), theSign);
    }
    
    @Override
    protected Widget createRightPanel() {
        return new ContainerWidget().inlineStyle("""
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        """)
                .addChild(createTextInput(IComponent.literal("The Text: "), () -> this.display, s -> this.display = s))
                .addChild(createTextInput(IComponent.literal("The  URL: "), () -> this.url, s -> this.url = s));
    }
    
    public Widget createTextInput(IComponent text, Supplier<String> getter, Consumer<String> setter) {
        var editBox = WidgetWrapper.editBox("",11514,setter);
        ((EditBox) editBox.getWidget()).setValue(getter.get());
        ((EditBox) editBox.getWidget()).displayPos = 0;
        return new ContainerWidget()
                .inlineStyle("""
                        size: 70% 20rpx;
                        margin-top: 2rpx;
                        margin-bottom: 2rpx;
                        flex-shrink: 0;
                        """)
                .addChild(new Label(text).inlineStyle("""
                        text-scale: expand-width;
                        text-align: left;
                        text-height: 10rpx;
                        text-color: -1;
                        """))
                .addChild(editBox.inlineStyle("width: 80%; margin-left: 2rpx;"));
    }
    
    @Override
    protected void writeBackToBE() {
        super.writeBackToBE();
        sign.displayContent = Component.literal(display);
        sign.url = url;
    }
    
}
