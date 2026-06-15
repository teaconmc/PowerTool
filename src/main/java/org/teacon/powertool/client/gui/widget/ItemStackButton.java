package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ItemStackButton extends Button {
    
    private ItemStack stack = ItemStack.EMPTY;
    
    public ItemStackButton(Builder builder) {
        super(builder);
    }
    
    public void setStack(ItemStack stack) {
        this.stack = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        this.setMessage(this.stack.isEmpty() ? Component.translatable("powertool.setcommand.gui.label.empty") : this.stack.getHoverName());
    }
    
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int color = this.isHovered ? 0x88FFFFFF : 0x55FFFFFF;
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x66000000);
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, color);
        graphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, color);
        graphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, color);
        graphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, color);
        if (!this.stack.isEmpty()) {
            int x = this.getX() + (this.width - 16) / 2;
            int y = this.getY() + (this.height - 16) / 2;
            graphics.item(this.stack, x, y);
            graphics.itemDecorations(Minecraft.getInstance().font, this.stack, x, y);
        }
    }
}
