package org.teacon.powertool.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class CommandRuneLabelSelectScreen extends Screen {
    
    private static final int SLOT_SIZE = 20;
    private static final int PADDING = 12;
    private final Screen parent;
    private final Consumer<ItemStack> callback;
    private final List<ItemStack> items = new ArrayList<>();
    private int scrollRow;
    
    public CommandRuneLabelSelectScreen(Screen parent, Consumer<ItemStack> callback) {
        super(Component.translatable("powertool.setcommand.gui.label"));
        this.parent = parent;
        this.callback = callback;
        this.items.add(ItemStack.EMPTY);
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (!stack.isEmpty()) {
                this.items.add(stack);
            }
        }
    }
    
    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.scrollRow = Math.max(0, this.scrollRow - this.rows()))
                .bounds(this.width / 2 - 76, this.height - 26, 20, 20)
                .build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 26, 100, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.scrollRow = Math.min(this.maxScrollRow(), this.scrollRow + this.rows()))
                .bounds(this.width / 2 + 56, this.height - 26, 20, 20)
                .build());
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int columns = this.columns();
        int rows = this.rows();
        int left = this.left(columns);
        int top = PADDING;
        int start = this.scrollRow * columns;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int index = start + row * columns + column;
                if (index >= this.items.size()) {
                    return;
                }
                int x = left + column * SLOT_SIZE;
                int y = top + row * SLOT_SIZE;
                boolean hovered = mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;
                int color = hovered ? 0x88FFFFFF : 0x55FFFFFF;
                graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x66000000);
                graphics.fill(x, y, x + SLOT_SIZE, y + 1, color);
                graphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, color);
                graphics.fill(x, y, x + 1, y + SLOT_SIZE, color);
                graphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, color);
                ItemStack stack = this.items.get(index);
                if (!stack.isEmpty()) {
                    graphics.item(stack, x + 2, y + 2);
                    graphics.itemDecorations(this.font, stack, x + 2, y + 2);
                }
            }
        }
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int index = this.indexAt(mouseX, mouseY);
        if (index >= 0 && index < this.items.size()) {
            this.callback.accept(this.items.get(index).copyWithCount(1));
            this.onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
    
    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
    
    private int indexAt(double mouseX, double mouseY) {
        int columns = this.columns();
        int rows = this.rows();
        int left = this.left(columns);
        int top = PADDING;
        if (mouseX < left || mouseY < top) {
            return -1;
        }
        int column = ((int) mouseX - left) / SLOT_SIZE;
        int row = ((int) mouseY - top) / SLOT_SIZE;
        if (column < 0 || column >= columns || row < 0 || row >= rows) {
            return -1;
        }
        return this.scrollRow * columns + row * columns + column;
    }
    
    private int columns() {
        return Math.max(1, (this.width - PADDING * 2) / SLOT_SIZE);
    }
    
    private int rows() {
        return Math.max(1, (this.height - 42) / SLOT_SIZE);
    }
    
    private int left(int columns) {
        return (this.width - columns * SLOT_SIZE) / 2;
    }
    
    private int maxScrollRow() {
        int columns = this.columns();
        int rows = this.rows();
        return Math.max(0, (this.items.size() + columns - 1) / columns - rows);
    }
}
