package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;

import java.util.List;
import java.util.function.Consumer;

@NonNullByDefault
public class SpriteGridWidget extends AbstractWidget {

    private static final int CELL_SIZE = 44;
    private static final int SPRITE_SIZE = 32;
    private final Consumer<Identifier> clickHandler;
    private List<Identifier> sprites = List.of();
    private int scrollRow;

    public SpriteGridWidget(int x, int y, int width, int height, Consumer<Identifier> clickHandler) {
        super(x, y, width, height, Component.empty());
        this.clickHandler = clickHandler;
    }

    public void setSprites(List<Identifier> sprites) {
        this.sprites = List.copyOf(sprites);
        this.scrollRow = 0;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        int columns = this.columns();
        int visibleRows = this.visibleRows();
        int firstIndex = this.scrollRow * columns;
        int lastIndex = Math.min(this.sprites.size(), firstIndex + visibleRows * columns);
        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        for (int index = firstIndex; index < lastIndex; index++) {
            int visibleIndex = index - firstIndex;
            int column = visibleIndex % columns;
            int row = visibleIndex / columns;
            int cellX = this.getX() + column * CELL_SIZE;
            int cellY = this.getY() + row * CELL_SIZE;
            boolean hovered = mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE;
            int borderColor = hovered ? 0xFFFFFFFF : 0xFF555555;
            graphics.fill(cellX, cellY, cellX + CELL_SIZE - 2, cellY + CELL_SIZE - 2, 0xFF242424);
            graphics.fill(cellX, cellY, cellX + CELL_SIZE - 2, cellY + 1, borderColor);
            graphics.fill(cellX, cellY + CELL_SIZE - 3, cellX + CELL_SIZE - 2, cellY + CELL_SIZE - 2, borderColor);
            graphics.fill(cellX, cellY, cellX + 1, cellY + CELL_SIZE - 2, borderColor);
            graphics.fill(cellX + CELL_SIZE - 3, cellY, cellX + CELL_SIZE - 2, cellY + CELL_SIZE - 2, borderColor);
            var sprite = atlas.getSprite(this.sprites.get(index));
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, cellX + 5, cellY + 5, SPRITE_SIZE, SPRITE_SIZE);
        }
        this.extractScrollbar(graphics);
        graphics.disableScissor();
        Identifier hoveredSprite = this.spriteAt(mouseX, mouseY);
        if (hoveredSprite == null) {
            this.setMessage(Component.empty());
        } else {
            Component location = Component.literal(hoveredSprite.toString());
            this.setMessage(location);
            graphics.setTooltipForNextFrame(location, mouseX, mouseY);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        Identifier sprite = this.spriteAt(event.x(), event.y());
        if (sprite != null) {
            this.clickHandler.accept(sprite);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.isMouseOver(mouseX, mouseY) || scrollY == 0.0) {
            return false;
        }
        int direction = scrollY > 0.0 ? -1 : 1;
        this.scrollRow = Math.max(0, Math.min(this.maxScrollRow(), this.scrollRow + direction));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

    private void extractScrollbar(GuiGraphicsExtractor graphics) {
        int maxScrollRow = this.maxScrollRow();
        if (maxScrollRow == 0) {
            return;
        }
        int totalRows = this.totalRows();
        int barHeight = Math.max(16, this.height * this.visibleRows() / totalRows);
        int trackHeight = this.height - barHeight;
        int barY = this.getY() + trackHeight * this.scrollRow / maxScrollRow;
        graphics.fill(this.getRight() - 3, this.getY(), this.getRight(), this.getBottom(), 0xFF1A1A1A);
        graphics.fill(this.getRight() - 3, barY, this.getRight(), barY + barHeight, 0xFFAAAAAA);
    }

    private @Nullable Identifier spriteAt(double mouseX, double mouseY) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return null;
        }
        int column = ((int) mouseX - this.getX()) / CELL_SIZE;
        int row = ((int) mouseY - this.getY()) / CELL_SIZE;
        if (column < 0 || column >= this.columns() || row < 0 || row >= this.visibleRows()) {
            return null;
        }
        int index = (this.scrollRow + row) * this.columns() + column;
        return index >= 0 && index < this.sprites.size() ? this.sprites.get(index) : null;
    }

    private int columns() {
        return Math.max(1, (this.width - 4) / CELL_SIZE);
    }

    private int visibleRows() {
        return Math.max(1, this.height / CELL_SIZE);
    }

    private int totalRows() {
        return Math.max(1, (this.sprites.size() + this.columns() - 1) / this.columns());
    }

    private int maxScrollRow() {
        return Math.max(0, this.totalRows() - this.visibleRows());
    }
}
