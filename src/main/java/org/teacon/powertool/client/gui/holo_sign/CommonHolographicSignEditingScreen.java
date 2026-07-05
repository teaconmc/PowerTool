/*
 * Parts of this Java source file are from GlowCase project, maintained by ModFest team,
 * licensed under CC0-1.0 per its repository.
 * You may find the original code at https://github.com/ModFest/glowcase
 */
package org.teacon.powertool.client.gui.holo_sign;

import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import dev.vfyjxf.taffy.style.TextAlign;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;
import org.teacon.powertool.client.gui.widget.AutoWidthMultiLineEditBoxWrapper;
import org.teacon.powertool.client.gui.widget.MultiLineEditBox;

import java.util.Arrays;

@NonNullByDefault
public class CommonHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<CommonHolographicSignBlockEntity> {
    
    public static final int MAXIMUM_LINE_COUNT = 10;
    public static final int MAXIMUM_LINE_LENGTH = 256;
    private static final int MINIMUM_EDIT_BOX_WIDTH = 120;
    
    private String @Nullable [] messages;
    private @Nullable MultiLineEditBox editBox;
    
    public CommonHolographicSignEditingScreen(CommonHolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit"), theSign);
    }
    
    @Override
    protected void readData(CommonHolographicSignBlockEntity theSign) {
        super.readData(theSign);
        var size = theSign.contents.size();
        this.messages = new String[size];
        for (int i = 0; i < size; i++) {
            messages[i] = theSign.contents.get(i).getString();
        }
    }
    
    @Override
    protected Widget createRightPanel() {
        this.editBox = new MultiLineEditBox();
        this.editBox.setMaxLines(MAXIMUM_LINE_COUNT);
        this.editBox.setMaxLineLength(MAXIMUM_LINE_LENGTH);
        var editBoxWrapper = new AutoWidthMultiLineEditBoxWrapper(this.editBox, MINIMUM_EDIT_BOX_WIDTH);
        if(this.messages != null) editBox.setValue(String.join("\n", messages));
        editBoxWrapper.setValueListener(s -> {
            if (s.isEmpty()) {
                this.messages = new String[0];
            } else {
                this.messages = s.split("\n", -1);
            }
        });
        return new ContainerWidget().inlineStyle("""
                        size: 100% 100%;
                        flex-direction: column;
                        align-items: flex-start;
                        justify-content: center;
                        overflow-x: scroll;
                        scrollbar-width: 8;
                        """)
                .addChild(editBoxWrapper.inlineStyle("width: auto; min-width: 50%; height: 148rpx; flex-shrink: 0;"));
    }
    
    @Override
    protected void writeBackToBE() {
        super.writeBackToBE();
        var toSend = this.messages == null ? new String[0] : Arrays.copyOfRange(this.messages, 0, this.messages.length);
        this.sign.contents = Arrays.stream(toSend).map(Component::literal).limit(MAXIMUM_LINE_COUNT).toList();
    }
    
    @Override
    public void onTextAlignChange() {
        if(this.editBox == null) return;
        this.editBox.setTextAlign(switch (this.textAlign){
            case LEFT -> TextAlign.LEFT;
            case CENTER -> TextAlign.CENTER;
            case RIGHT -> TextAlign.RIGHT;
        });
    }
}
