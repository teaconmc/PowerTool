package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
public class ConfigurationHierarchy extends EditorWindow {

    private static final int INDENT = 12;
    private static final int TOGGLE_SIZE = 8;
    private static final int TOGGLE_MARGIN = 4;

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

    public ConfigurationHierarchy(int width, int height) {
        super(0, 0, width, height, Component.literal("Hierarchy"));
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
        var height = this.visible.size() * Entry.HEIGHT;
        this.updateScrollHeight(height);
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
    protected boolean skipRender() {
        return this.visible.isEmpty();
    }

    @Override
    protected void extractInnerRenderState(
            final GuiGraphicsExtractor  graphics,
            int                         localMouseX,
            int                         localMouseY,
            int                         innerWidth,
            int                         innerHeight,
            float                       partialTick
    ) {
        final var pose = graphics.pose();
        final var font = Minecraft.getInstance().font;

        int offset = (int) this.getOffset();
        pose.translate(0, -offset);

        int y = 0;
        for (Entry entry : this.visible) {
            if (y + Entry.HEIGHT > offset && y < offset + this.height) {
                int indentX = entry.depth * INDENT;
                boolean entryHovered = this.isHovered
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
                    this.drawChevron(
                            graphics,
                            font,
                            toggleX, toggleY,
                            toggleColor,
                            this.expanded.contains(entry.configuration)
                    );
                }

                int textX = indentX + (entry.hasChildren ? TOGGLE_SIZE + TOGGLE_MARGIN : COLOR_BAR_WIDTH + TOGGLE_MARGIN);
                int textY = y + (Entry.HEIGHT - 9) / 2 + 1;
                graphics.text(font, entry.configuration.name(),
                        textX, textY, isSelected ? 0xffffffff : 0xffe0e0e0);
            }

            y += Entry.HEIGHT;
        }

    }

    private void drawChevron(
            GuiGraphicsExtractor graphics,
            Font font,
            int x,
            int y,
            int color,
            boolean expanded
    ) {
        if (expanded) {
            graphics.text(font, "▼", x, y + 1, color, false);
        } else {
            graphics.text(font, "▶", x, y, color, false);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {

        if (!this.isHovered || this.visible.isEmpty()) {
            return false;
        }

        var remapped    = this.remap(event);
        var localX      = remapped.x();
        var localY      = remapped.y();

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

    public record Entry(ExhibitionNode configuration, int depth, boolean hasChildren) {
        public static final int HEIGHT = 12;
    }
}
