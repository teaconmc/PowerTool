package org.teacon.powertool.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.teacon.powertool.client.gui.widget.ButtonWithHighlight;
import org.teacon.powertool.client.gui.widget.InvisibleButton;
import org.teacon.powertool.menu.PowerSupplyMenu;
import org.teacon.powertool.network.server.UpdatePowerSupplyData;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PowerSupplyScreen extends AbstractContainerScreen<PowerSupplyMenu> {
    
    private static final Identifier BG_LOCATION = VanillaUtils.modRL("textures/gui/power_supply.png");
    
    private EditBox input;
    private ButtonWithHighlight minus, plus;
    private int status = 1, power = -1;
    
    public PowerSupplyScreen(PowerSupplyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.status = menu.dataHolder.status;
        this.power = menu.dataHolder.power;
        this.imageWidth = 170;
        this.imageHeight = 105;
    }
    
    public void onToggled(Button toggle) {
        this.status = this.status == 0 ? 1 : 0;
        ClientPacketDistributor.sendToServer(new UpdatePowerSupplyData(0, this.status));
    }
    
    public void updatePowerOutput() {
        ClientPacketDistributor.sendToServer(new UpdatePowerSupplyData(1, this.power));
    }
    
    @Override
    protected void init() {
        super.init();
        // The minus button
        this.minus = this.addRenderableWidget(new ButtonWithHighlight(new Button.Builder(Component.empty(), btn -> this.input.setValue(Integer.toString(--this.power)))
                .pos(this.leftPos + 9, this.topPos + 44)
                .size(16, 16),
                btn -> updatePowerOutput(), BG_LOCATION, 256, 256, 170, 44, 170, 60, 170, 76));
        // The plus button
        this.plus = this.addRenderableWidget(new ButtonWithHighlight(new Button.Builder(Component.empty(), btn -> this.input.setValue(Integer.toString(++this.power)))
                .pos(this.leftPos + 145, this.topPos + 44)
                .size(16, 16),
                btn -> updatePowerOutput(), BG_LOCATION, 256, 256, 186, 44, 186, 60, 186, 76));
        this.addRenderableWidget(new InvisibleButton(
                new Button.Builder(Component.empty(), this::onToggled)
                        .pos(this.leftPos + 125, this.topPos + 20)
                        .size(32, 13)
        ));
        // The input field
        this.input = new EditBox(this.font, this.leftPos + 32, this.topPos + 48, 100, 16, Component.empty());
        this.input.setCanLoseFocus(false);
        this.input.setTextColor(-1);
        this.input.setTextColorUneditable(-1);
        this.input.setBordered(false);
        this.input.setMaxLength(11);
        this.input.setResponder(newValue -> {
            try {
                this.power = Integer.parseInt(newValue);
                this.input.setTextColor(-1);
            } catch (Exception e) {
                this.input.setTextColor(0xFFFF0000);
            }
        });
        this.input.setValue(Integer.toString(this.power));
        this.addWidget(this.input);
        this.setInitialFocus(this.input);
    }
    
    @Override
    protected void containerTick() {
        super.containerTick();
        this.minus.tick();
        this.plus.tick();
    }
    
    @Override
    public void resize(int width, int height) {
        String s = this.input.getValue();
        super.resize(width, height);
        this.input.setValue(s);
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        var keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            var p = this.minecraft.player;
            if (p != null) {
                p.closeContainer();
            }
        }
        return this.input.keyPressed(event)
                || this.input.canConsumeInput()
                || super.keyPressed(event);
    }
    
    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BG_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}
