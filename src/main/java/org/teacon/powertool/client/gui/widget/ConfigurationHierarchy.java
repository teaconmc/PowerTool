package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.teacon.powertool.exhibition.node.ExhibitionNode;

import org.teacon.powertool.exhibition.node.ModelPartNode;
import org.teacon.powertool.exhibition.node.SkinNode;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class ConfigurationHierarchy extends AbstractWidget {

    private static final int INDENT = 12;
    private static final int TOGGLE_SIZE = 8;
    private static final int TOGGLE_MARGIN = 2;

    private static final int COLOR_HUMANOID = 0xff66cc66;
    private static final int COLOR_MODEL_PART = 0xff6699cc;
    private static final int COLOR_SKIN = 0xffcc9966;
    private static final int COLOR_DEFAULT = 0xff888888;

    private static final int COLOR_BAR_WIDTH = 3;

    private ExhibitionNode root;
    private ExhibitionNode selected;

    private final List<Entry> visible = new ArrayList<>();
    private final Set<ExhibitionNode> expanded = new HashSet<>();

    private Consumer<ExhibitionNode> onSelect;

    private int scroll = 0;
    private int available = 0;
    private float ratio = 1.0f;

    public ConfigurationHierarchy(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public void setRoot(ExhibitionNode root) {
        this.root = root;
        this.rebuildFlatList();
    }

    public ExhibitionNode getRoot() {
        return this.root;
    }

    public void setOnSelect(Consumer<ExhibitionNode> onSelect) {
        this.onSelect = onSelect;
    }

    public ExhibitionNode getSelected() {
        return this.selected;
    }

    private static int typeColor(ExhibitionNode node) {
        if (node instanceof ModelPartNode) return COLOR_MODEL_PART;
        if (node instanceof SkinNode) return COLOR_SKIN;
        return COLOR_DEFAULT;
    }

    private void rebuildFlatList() {
        this.visible.clear();
        if (this.root != null) {
            this.collect(this.root, 0);
        }
    }

    private void collect(ExhibitionNode config, int depth) {
        boolean hasChildren = !config.children().isEmpty();
        this.visible.add(new Entry(config, depth, hasChildren));
        if (hasChildren && this.expanded.contains(config)) {
            for (ExhibitionNode child : config.children()) {
                this.collect(child, depth + 1);
            }
        }
    }

    @Override
    protected void extractWidgetRenderState(
            final GuiGraphicsExtractor graphics,
            final int mouseX,
            final int mouseY,
            final float partialTick
    ) {
        graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x88000000);
        graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xffffffff);

        if (this.visible.isEmpty()) {
            return;
        }

        int totalHeight = this.visible.size() * Entry.HEIGHT;

        boolean hovered = this.isMouseOver(mouseX, mouseY);
        Minecraft minecraft = Minecraft.getInstance();

        var pose = graphics.pose();
        pose.pushMatrix();
        int offset = (int) (this.scroll / this.ratio);
        pose.translate(this.getX(), this.getY());

        graphics.enableScissor(0, 0, this.width, this.height);

        pose.translate(0, -offset);

        int localMouseX = mouseX - this.getX();
        int localMouseY = mouseY - this.getY() + offset;

        int y = 0;
        for (Entry entry : this.visible) {
            if (y + Entry.HEIGHT > offset && y < offset + this.height) {
                int indentX = entry.depth * INDENT;
                boolean entryHovered = hovered
                        && localMouseY > y && localMouseY < y + Entry.HEIGHT
                        && localMouseX > 0 && localMouseX < this.width;

                boolean isSelected = entry.configuration.equals(this.selected);

                if (isSelected) {
                    graphics.fill(0, y, this.width, y + Entry.HEIGHT, 0x3377ccff);
                } else if (entryHovered) {
                    graphics.fill(0, y, this.width, y + Entry.HEIGHT, 0x33ffffff);
                }

                int typeColor = typeColor(entry.configuration);
                graphics.fill(0, y, COLOR_BAR_WIDTH, y + Entry.HEIGHT, typeColor);

                if (entry.hasChildren) {
                    int toggleX = indentX + TOGGLE_MARGIN;
                    int toggleY = y + (Entry.HEIGHT - TOGGLE_SIZE) / 2;
                    boolean toggleHovered = entryHovered && localMouseX > toggleX && localMouseX < toggleX + TOGGLE_SIZE;
                    int toggleColor = toggleHovered ? 0xffffff00 : 0xffaaaaaa;
                    this.drawChevron(graphics, toggleX, toggleY, toggleColor,
                            this.expanded.contains(entry.configuration));
                }

                int textX = indentX + (entry.hasChildren ? TOGGLE_SIZE + TOGGLE_MARGIN * 2 : COLOR_BAR_WIDTH + TOGGLE_MARGIN);
                int textY = y + (Entry.HEIGHT - 9) / 2;
                graphics.text(minecraft.font, entry.configuration.name(),
                        textX, textY, isSelected ? 0xffffffff : 0xffe0e0e0);
            }

            y += Entry.HEIGHT;
        }

        graphics.disableScissor();
        pose.popMatrix();

        this.drawScrollbar(graphics, totalHeight);
    }

    private void drawChevron(GuiGraphicsExtractor g, int x, int y, int color, boolean expanded) {
        int s = TOGGLE_SIZE;
        int h = s / 2;
        if (expanded) {
            for (int i = 0; i < h; i++) {
                g.fill(x + i, y + i, x + s - i, y + i + 1, color);
            }
        } else {
            for (int i = 0; i < h; i++) {
                g.fill(x + i, y + i, x + i + 1, y + s - i, color);
            }
        }
    }

    private void drawScrollbar(final GuiGraphicsExtractor graphics, int totalHeight) {
        int channelHeight = this.height - 4;
        this.ratio = Math.min((float) this.height / totalHeight, 1.0f);
        int sliderHeight = Math.max((int) (this.ratio * channelHeight), 8);
        this.available = channelHeight - sliderHeight;

        int top = this.getY() + 2;
        int left = this.getX() + this.width - 4;

        int sliderOffset = this.available > 0 ? this.scroll : 0;

        graphics.fill(left - 1, top, left + 3, top + 1, 0xffffffff);
        graphics.fill(left, top + 2 + sliderOffset, left + 2,
                top + 2 + sliderHeight + sliderOffset, 0xffffffff);
        graphics.fill(left - 1, top + 3 + channelHeight, left + 3, top + 4 + channelHeight, 0xffffffff);
        this.scroll = Math.clamp(this.scroll, 0, Math.max(this.available, 0));
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        var mx = event.x();
        var my = event.y();

        if (!this.isMouseOver(mx, my) || this.visible.isEmpty()) {
            return false;
        }

        int localX = (int) mx - this.getX();
        int localY = (int) my - this.getY() + (this.ratio < 1.0f ? (int) (this.scroll / this.ratio) : 0);

        int y = 0;
        for (Entry entry : this.visible) {
            if (localY > y && localY < y + Entry.HEIGHT) {
                if (entry.hasChildren) {
                    int toggleX = entry.depth * INDENT + TOGGLE_MARGIN;
                    if (localX > toggleX && localX < toggleX + TOGGLE_SIZE) {
                        if (this.expanded.contains(entry.configuration)) {
                            this.expanded.remove(entry.configuration);
                        } else {
                            this.expanded.add(entry.configuration);
                        }
                        this.rebuildFlatList();
                        return true;
                    }
                }
                this.selected = entry.configuration;
                if (this.onSelect != null) {
                    this.onSelect.accept(entry.configuration);
                }
                return true;
            }
            y += Entry.HEIGHT;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        this.scroll -= (int) scrollY * 4;
        this.scroll = Math.clamp(this.scroll, 0, Math.max(this.available, 0));
        return true;
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput output) {
    }

    public record Entry(ExhibitionNode configuration, int depth, boolean hasChildren) {
        public static final int HEIGHT = 16;
    }
}
