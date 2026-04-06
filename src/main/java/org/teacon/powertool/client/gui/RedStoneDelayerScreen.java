package org.teacon.powertool.client.gui;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.block.entity.RedStoneDelayBlockEntity;
import org.teacon.powertool.client.gui.widget.ObjectInputBox;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RedStoneDelayerScreen extends Screen {
    
    private static final Identifier TEXTURE_RISING_EDGE = VanillaUtils.modRL("delayer_button_rising_edge");
    private static final Identifier TEXTURE_DESCENDING_EDGE = VanillaUtils.modRL("delayer_button_descending_edge");
    private static final Identifier TEXTURE_IGNORE = VanillaUtils.modRL("delayer_button_ignore");
    private static final Identifier TEXTURE_RESET = VanillaUtils.modRL("delayer_button_reset");
    
    protected final RedStoneDelayBlockEntity te;
    protected ObjectInputBox<Integer> delayInput;
    protected RedStoneDelayBlockEntity.Mode mode;
    protected boolean checkRisingEdge;
    
    public RedStoneDelayerScreen(RedStoneDelayBlockEntity te) {
        super(Component.literal("delayer"));
        this.te = te;
    }
    
    @Override
    protected void init() {
        var mc = Minecraft.getInstance();
        var font = mc.font;
        var box_l = (int) Math.max(100, width * 0.2);
        var startY = (int) (height * 0.15);
        mode = te.mode;
        checkRisingEdge = te.checkRisingEdge;
        
        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, btn -> this.onDone())
                .pos(this.width / 2 - 100, this.height / 2 + startY)
                .size(200, 20).build());
        
        this.delayInput = new ObjectInputBox<>(font, this.width / 2 - box_l / 2, height / 2, box_l, 20, Component.literal("Delay Ticks"), ObjectInputBox.INT_VALIDATOR, ObjectInputBox.INT_RESPONDER);
        this.delayInput.setMaxLength(6);
        this.delayInput.setValue(String.valueOf(te.delayTicks));
        
        var buttonCheckRisingEdge = new SpriteIconButton.Builder(Component.empty(), (btn) -> checkRisingEdge = true, true)
                .sprite(TEXTURE_RISING_EDGE, 16, 16)
                .size(20, 20)
                .build();
        buttonCheckRisingEdge.setPosition(this.width / 2 - 22, height / 2 - 50);
        buttonCheckRisingEdge.setTooltip(Tooltip.create(Component.translatable("powertool.gui.delayer.check_rising_edge")));
        var buttonCheckDescendingEdge = new SpriteIconButton.Builder(Component.empty(), (btn) -> checkRisingEdge = false, true)
                .sprite(TEXTURE_DESCENDING_EDGE, 16, 16)
                .size(20, 20)
                .build();
        buttonCheckDescendingEdge.setPosition(this.width / 2 + 3, height / 2 - 50);
        buttonCheckDescendingEdge.setTooltip(Tooltip.create(Component.translatable("powertool.gui.delayer.check_descending_edge")));
        var buttonIgnore = new SpriteIconButton.Builder(Component.empty(), (btn) -> mode = RedStoneDelayBlockEntity.Mode.IGNORE, true)
                .sprite(TEXTURE_IGNORE, 16, 16)
                .size(20, 20)
                .build();
        buttonIgnore.setPosition(this.width / 2 - 22, height / 2 - 25);
        buttonIgnore.setTooltip(Tooltip.create(Component.translatable("powertool.gui.delayer.ignore")));
        var buttonReset = new SpriteIconButton.Builder(Component.empty(), (btn) -> mode = RedStoneDelayBlockEntity.Mode.RESET, true)
                .sprite(TEXTURE_RESET, 16, 16)
                .size(20, 20)
                .build();
        buttonReset.setPosition(this.width / 2 + 3, height / 2 - 25);
        buttonReset.setTooltip(Tooltip.create(Component.translatable("powertool.gui.delayer.reset")));
        this.addRenderableWidget(this.delayInput);
        this.addRenderableWidget(buttonCheckRisingEdge);
        this.addRenderableWidget(buttonCheckDescendingEdge);
        this.addRenderableWidget(buttonIgnore);
        this.addRenderableWidget(buttonReset);
        super.init();
    }
    
    protected void onDone() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
    
    @Override
    public void removed() {
        if (this.delayInput == null) return;
        te.delayTicks = Objects.requireNonNullElse(delayInput.get(), 0);
        te.mode = mode;
        te.checkRisingEdge = checkRisingEdge;
        PacketDistributor.sendToServer(UpdateBlockEntityData.create(te));
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        var currentMode1 = Component.translatable("powertool.gui.delayer.mode1");
        var currentMode2 = Component.translatable("powertool.gui.delayer.mode2");
        guiGraphics.drawString(font, currentMode1, width / 2 - 50 - font.width(currentMode1), height / 2 - 50 + 6, -1);
        guiGraphics.drawString(font, currentMode2, width / 2 - 50 - font.width(currentMode2), height / 2 - 25 + 6, -1);
        guiGraphics.blitSprite(mode == RedStoneDelayBlockEntity.Mode.IGNORE ? TEXTURE_IGNORE : TEXTURE_RESET, width / 2 - 43, height / 2 - 25 + 2, 16, 16);
        guiGraphics.blitSprite(checkRisingEdge ? TEXTURE_RISING_EDGE : TEXTURE_DESCENDING_EDGE, width / 2 - 43, height / 2 - 50 + 2, 16, 16);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
