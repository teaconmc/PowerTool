package org.teacon.powertool.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.teacon.powertool.menu.PowerSupplyMenu;
import org.teacon.powertool.network.server.UpdatePowerSupplyData;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PowerSupplyScreen extends AbstractContainerScreen<PowerSupplyMenu> {

    private static final ResourceLocation BG_LOCATION = VanillaUtils.modResourceLocation("textures/gui/power_supply.png");

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
        PacketDistributor.sendToServer(new UpdatePowerSupplyData(0, this.status));
    }

    public void updatePowerOutput() {
        PacketDistributor.sendToServer(new UpdatePowerSupplyData(1, this.power));
    }

    @Override
    protected void init() {
        super.init();
        // The minus button
        this.minus = this.addRenderableWidget(new ButtonWithHighlight(new Button.Builder(Component.empty(), btn -> this.input.setValue(Integer.toString(--this.power)))
                .pos(this.leftPos + 9, this.topPos + 44)
                .size( 16, 16),
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
    public void resize(Minecraft mc, int pWidth, int pHeight) {
        String s = this.input.getValue();
        super.resize(mc, pWidth, pHeight);
        this.input.setValue(s);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            var mc = this.minecraft;
            if (mc != null) {
                var p = mc.player;
                if (p != null) {
                    p.closeContainer();
                }
            }
        }
        return this.input.keyPressed(keyCode, scanCode, modifiers)
                || this.input.canConsumeInput()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics,mouseX,mouseY,partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.disableBlend();
        this.input.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.blit(BG_LOCATION, 125, 0, this.status == 0 ? 202 : 170, 0, 32, 44);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BG_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
