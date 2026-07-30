package org.teacon.powertool.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.teacon.powertool.annotation.NonNullByDefault;

import java.util.function.Consumer;

@NonNullByDefault
public class CommandRuneLabelSelectScreen extends Screen {

    private static final int SLOT_SIZE = 20;
    private static final int BUTTON_WIDTH = 80;
    private final Screen parent;
    private final Consumer<ItemStack> callback;
    private ItemStack labelStack;

    public CommandRuneLabelSelectScreen(Screen parent, ItemStack labelStack, Consumer<ItemStack> callback) {
        super(Component.translatable("powertool.setcommand.gui.label"));
        this.parent = parent;
        this.labelStack = labelStack.isEmpty() ? ItemStack.EMPTY : labelStack.copyWithCount(1);
        this.callback = callback;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds((this.width - BUTTON_WIDTH) / 2, this.slotY() + SLOT_SIZE + 20, BUTTON_WIDTH, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        Component instruction = Component.translatable("powertool.setcommand.gui.label.jei_hint");
        int slotX = this.slotX();
        int slotY = this.slotY();
        int color = this.isOverSlot(mouseX, mouseY) ? 0xFFFFFFFF : 0xFFAAAAAA;
        graphics.text(this.font, instruction, this.width / 2 - this.font.width(instruction) / 2, slotY - 20, 0xFFFFFFFF);
        graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x66000000);
        graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + 1, color);
        graphics.fill(slotX, slotY + SLOT_SIZE - 1, slotX + SLOT_SIZE, slotY + SLOT_SIZE, color);
        graphics.fill(slotX, slotY, slotX + 1, slotY + SLOT_SIZE, color);
        graphics.fill(slotX + SLOT_SIZE - 1, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, color);
        if (!this.labelStack.isEmpty()) {
            graphics.item(this.labelStack, slotX + 2, slotY + 2);
            graphics.itemDecorations(this.font, this.labelStack, slotX + 2, slotY + 2);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.isOverSlot(event.x(), event.y()) && !this.labelStack.isEmpty()) {
            this.acceptLabel(ItemStack.EMPTY);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    public int slotX() {
        return (this.width - SLOT_SIZE) / 2;
    }

    public int slotY() {
        return (this.height - SLOT_SIZE) / 2;
    }

    public int slotSize() {
        return SLOT_SIZE;
    }

    public void acceptLabel(ItemStack stack) {
        this.labelStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        this.callback.accept(this.labelStack);
    }

    private boolean isOverSlot(double mouseX, double mouseY) {
        int slotX = this.slotX();
        int slotY = this.slotY();
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }
}
