package org.teacon.powertool.client.gui.holo_sign;

import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import dev.vfyjxf.taffy.style.TextAlign;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.BBCodeHolographicSignBlockEntity;
import org.teacon.powertool.client.gui.widget.AutoWidthMultiLineEditBoxWrapper;
import org.teacon.powertool.client.gui.widget.MultiLineEditBox;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * GUI screen for editing BBC Holographic Sign content.
 * Uses parent class for most settings (color, scale, alignment, etc.).
 * BBC-specific settings:
 * - Min width (blocks)
 * Fixed defaults (no GUI):
 * - Line height: 1.0
 * - Bold: false
 * - Underline: false
 * - Italic: false
 */
@NonNullByDefault
public class BBCodeHolographicSignEditingScreen extends BaseHolographicSignEditingScreen<BBCodeHolographicSignBlockEntity> {

    public static final int MAXIMUM_LINE_COUNT = 100;
    public static final int MAXIMUM_LINE_LENGTH = 256;
    private static final int MINIMUM_EDIT_BOX_WIDTH = 120;

    private String @Nullable [] messages;
    private @Nullable MultiLineEditBox editBox;

    // BBC-specific parameter: min width
    protected float bbcMinWidth;

    public BBCodeHolographicSignEditingScreen(BBCodeHolographicSignBlockEntity theSign) {
        super(Component.translatable("sign.edit.bbc"), theSign);
    }

    @Override
    protected void readData(BBCodeHolographicSignBlockEntity theSign) {
        super.readData(theSign);
        var size = theSign.rawContent.size();
        this.messages = new String[size];
        for (int i = 0; i < size; i++) {
            messages[i] = theSign.rawContent.get(i);
        }
        bbcMinWidth = theSign.minWidth;
    }

    @Override
    protected Widget createLeftPanel() {
        var basePanel = (ContainerWidget) super.createLeftPanel();

        // Add BBC-specific min width input
        basePanel.addChild(createFloatInput(
                0F, 10F, 0.1F,
                () -> IComponent.literal("Min Width (blocks)"),
                () -> bbcMinWidth,
                w -> bbcMinWidth = w
        ));

        return basePanel;
    }

    @Override
    protected Widget createRightPanel() {
        this.editBox = new MultiLineEditBox();
        this.editBox.setMaxLines(MAXIMUM_LINE_COUNT);
        this.editBox.setMaxLineLength(MAXIMUM_LINE_LENGTH);
        var editBoxWrapper = new AutoWidthMultiLineEditBoxWrapper(this.editBox, MINIMUM_EDIT_BOX_WIDTH);
        if (this.messages != null) editBox.setValue(String.join("\n", messages));
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
        sign.rawContent = new ArrayList<>(Arrays.asList(toSend));
        // Build forRender list
        sign.forRender.clear();
        for (var line : toSend) {
            sign.forRender.add(Component.literal(line));
        }
        // Use parent class values as defaults
        sign.defaultColor = this.color;
        sign.defaultScale = this.scale;
        sign.defaultAlign = switch (this.textAlign) {
            case LEFT -> BBCodeHolographicSignBlockEntity.TextAlign.LEFT;
            case CENTER -> BBCodeHolographicSignBlockEntity.TextAlign.CENTER;
            case RIGHT -> BBCodeHolographicSignBlockEntity.TextAlign.RIGHT;
        };
        // BBC-specific values
        sign.minWidth = bbcMinWidth;
        // Fixed defaults
        sign.defaultLineHeight = 1.0F;
        sign.defaultBold = false;
        sign.defaultUnderline = false;
        sign.defaultItalic = false;

        // Parse BBCodes after updating
        sign.parseBBCode(sign.forRender);
    }

    @Override
    public void onTextAlignChange() {
        if (this.editBox == null) return;
        this.editBox.setTextAlign(switch (this.textAlign) {
            case LEFT -> TextAlign.LEFT;
            case CENTER -> TextAlign.CENTER;
            case RIGHT -> TextAlign.RIGHT;
        });
    }
}
