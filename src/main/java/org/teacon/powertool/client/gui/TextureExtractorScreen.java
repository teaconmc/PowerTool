package org.teacon.powertool.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.client.gui.widget.SpriteGridWidget;
import org.teacon.powertool.menu.TextureExtractorMenu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NonNullByDefault
public class TextureExtractorScreen extends AbstractContainerScreen<TextureExtractorMenu> {

    private static final int FILTER_SLOT_SIZE = 22;
    private static final int MAX_GUI_WIDTH = 440;
    private static final int MAX_GUI_HEIGHT = 300;
    private ItemStack filterStack = ItemStack.EMPTY;
    private @Nullable SpriteGridWidget spriteGrid;

    public TextureExtractorScreen(TextureExtractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.imageWidth = Math.min(MAX_GUI_WIDTH, Math.max(220, this.width - 120));
        this.imageHeight = Math.min(MAX_GUI_HEIGHT, Math.max(180, this.height - 36));
        super.init();
        this.spriteGrid = this.addRenderableWidget(new SpriteGridWidget(
                this.leftPos + 8,
                this.topPos + 58,
                this.imageWidth - 16,
                this.imageHeight - 66
        ));
        this.refreshSprites();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xDD181818);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + 1, 0xFF777777);
        graphics.fill(this.leftPos, this.topPos + this.imageHeight - 1, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF555555);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + 1, this.topPos + this.imageHeight, 0xFF777777);
        graphics.fill(this.leftPos + this.imageWidth - 1, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF555555);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        Component instruction = Component.translatable("powertool.texture_extractor.filter_hint");
        int slotX = this.filterSlotX();
        int slotY = this.filterSlotY();
        int borderColor = this.isOverFilterSlot(mouseX, mouseY) ? 0xFFFFFFFF : 0xFFAAAAAA;
        graphics.text(this.font, instruction, this.width / 2 - this.font.width(instruction) / 2, this.topPos + 8, 0xFFFFFFFF);
        graphics.fill(slotX, slotY, slotX + FILTER_SLOT_SIZE, slotY + FILTER_SLOT_SIZE, 0xFF242424);
        graphics.fill(slotX, slotY, slotX + FILTER_SLOT_SIZE, slotY + 1, borderColor);
        graphics.fill(slotX, slotY + FILTER_SLOT_SIZE - 1, slotX + FILTER_SLOT_SIZE, slotY + FILTER_SLOT_SIZE, borderColor);
        graphics.fill(slotX, slotY, slotX + 1, slotY + FILTER_SLOT_SIZE, borderColor);
        graphics.fill(slotX + FILTER_SLOT_SIZE - 1, slotY, slotX + FILTER_SLOT_SIZE, slotY + FILTER_SLOT_SIZE, borderColor);
        if (!this.filterStack.isEmpty()) {
            graphics.item(this.filterStack, slotX + 3, slotY + 3);
            graphics.itemDecorations(this.font, this.filterStack, slotX + 3, slotY + 3);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.isOverFilterSlot(event.x(), event.y()) && !this.filterStack.isEmpty()) {
            this.setFilterStack(ItemStack.EMPTY);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        SpriteGridWidget grid = this.spriteGrid;
        if (grid != null && grid.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public int filterSlotX() {
        return (this.width - FILTER_SLOT_SIZE) / 2;
    }

    public int filterSlotY() {
        return this.topPos + 28;
    }

    public int filterSlotSize() {
        return FILTER_SLOT_SIZE;
    }

    public void setFilterStack(ItemStack stack) {
        if (!stack.isEmpty() && !(stack.getItem() instanceof BlockItem)) {
            return;
        }
        this.filterStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        this.refreshSprites();
    }

    private boolean isOverFilterSlot(double mouseX, double mouseY) {
        int slotX = this.filterSlotX();
        int slotY = this.filterSlotY();
        return mouseX >= slotX && mouseX < slotX + FILTER_SLOT_SIZE && mouseY >= slotY && mouseY < slotY + FILTER_SLOT_SIZE;
    }

    private void refreshSprites() {
        SpriteGridWidget grid = this.spriteGrid;
        if (grid == null) {
            return;
        }
        grid.setSprites(this.getFilteredSprites());
    }

    private List<Identifier> getFilteredSprites() {
        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        Set<Identifier> sprites;
        if (this.filterStack.getItem() instanceof BlockItem blockItem) {
            sprites = this.getModelSprites(blockItem);
        } else {
            sprites = atlas.getTextures().keySet();
        }
        return sprites.stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();
    }

    private Set<Identifier> getModelSprites(BlockItem blockItem) {
        var minecraft = Minecraft.getInstance();
        var model = minecraft.getModelManager().getBlockStateModelSet().get(blockItem.getBlock().defaultBlockState());
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(42L), parts);
        Set<Identifier> sprites = new HashSet<>();
        sprites.add(model.particleMaterial().sprite().contents().name());
        for (var part : parts) {
            sprites.add(part.particleMaterial().sprite().contents().name());
            part.getQuads(null).forEach(quad -> sprites.add(quad.materialInfo().sprite().contents().name()));
            for (Direction direction : Direction.values()) {
                part.getQuads(direction).forEach(quad -> sprites.add(quad.materialInfo().sprite().contents().name()));
            }
        }
        return sprites;
    }
}
