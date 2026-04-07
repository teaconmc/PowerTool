package org.teacon.powertool.client.gui.widget;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.teacon.powertool.client.gui.TextureExtractorScreen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TextureAtlasSpriteList extends EntryListWidget<TextureExtractorScreen, TextureAtlasSpriteList.Entry> {
    
    public TextureAtlasSpriteList(TextureExtractorScreen screen, int width, int height, int y, int itemHeight) {
        super(screen, width, height, y, itemHeight);
    }
    
    @Override
    void init(TextureExtractorScreen screen) {
        this.setX(10);
        update();
    }
    
    public void update() {
        var entries = entries().size();
        this.clearEntries();
        var list = screen.getFilteredTextures();
        var columns = (width - 25) / 25;
        var buf = new ArrayList<Identifier>();
        for (var i = 0; i < list.size() / columns; i++) {
            for (var j = 0; j < columns; j++) {
                buf.add(list.get(i * columns + j));
            }
            this.addEntry(new Entry(id, buf));
            buf.clear();
        }
        for (var i = 0; i < list.size() % columns; i++) {
            buf.add(list.get(list.size() / columns + i));
        }
        this.addEntry(new Entry(id, buf));
        if (entries != entries().size()) {
            this.setScrollAmount(0);
        }
    }
    
    @Override
    public int getRowWidth() {
        return width - 30;
    }
    
    public static class Entry extends EntryListWidget.Entry<Entry> {
        public int id;
        public final List<Identifier> textures = new ArrayList<>();
        protected final List<Button> spriteButtons = new ArrayList<>();
        
        public Entry(int id, List<Identifier> texture) {
            this.id = id;
            for (var rl : texture) {
                textures.add(rl);
                var button = new BlockSpriteButton(rl, -1, -1, 20, 20, Component.empty(), (b) -> Minecraft.getInstance().keyboardHandler.setClipboard(rl.toString()));
                button.setTooltip(Tooltip.create(Component.literal(rl.toString())));
                spriteButtons.add(button);
            }
        }
        
        @Override
        public void setID(int id) {
            this.id = id;
        }
        
        @Override
        public int getID() {
            return id;
        }
        
        @Override
        public Entry copyWithID(int id) {
            return new Entry(id, textures);
        }
        
        @Override
        public List<? extends NarratableEntry> narratables() {
            return spriteButtons;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return spriteButtons;
        }
        
        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            var i = 0;
            var left = this.getX();
            var top = this.getY();
            for (var button : spriteButtons) {
                button.setPosition(left + i * 25, top);
                button.extractRenderState(graphics, mouseX, mouseY, a);
                i += 1;
            }
        }
    }
    
    public static class BlockSpriteButton extends Button {
        public final Identifier texture;
        private final TextureAtlasSprite sprite;
        
        protected BlockSpriteButton(Identifier texture, int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
            this.texture = texture;
            //noinspection deprecation
            sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(texture);
        }
        
        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            if (this.isHoveredOrFocused()) {
                graphics.horizontalLine(getX(), getX() + getWidth(), getY(), -1);
                graphics.horizontalLine(getX(), getX() + getWidth(), getY() + getHeight(), -1);
                graphics.verticalLine(getX(), getY(), getY() + getHeight(), -1);
                graphics.verticalLine(getX() + getWidth(), getY(), getY() + getHeight(), -1);
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX() + 1, getY() + 1, 0, getWidth() - 1, getHeight() - 1);
        }
    }
}
