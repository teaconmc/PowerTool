package org.teacon.powertool.client.gui.widget;

import com.xkball.xklib.XKLib;
import com.xkball.xklib.api.gui.input.IMouseButtonEvent;
import com.xkball.xklib.ui.render.IGUIGraphics;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklib.x3d.api.render.ITextureAtlasSprite;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.client.gui.TextureExtractorScreen;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SpriteList extends ContainerWidget {
    
    private static final int CELL_SIZE = 40;
    private static final int SPRITE_SIZE = 34;
    
    private final TextureExtractorScreen screen;
    
    public SpriteList(TextureExtractorScreen screen) {
        this.screen = screen;
        this.inlineStyle("""
                flex-direction: row;
                overflow-y: scroll;
                scrollbar-width: 8;
                background-color: 0x55000000;
                align-content: start;
                justify-content: start;
                """);
        this.setStyle(style -> {
            style.flexWrap = FlexWrap.WRAP;
        });
        this.update();
    }
    
    public void update() {
        this.clearChildren();
        this.addChild(new SpriteCell(null));
        for (var texture : this.screen.getFilteredTextures()) {
            this.addChild(new SpriteCell(texture));
        }
    }
    
    public TextureExtractorScreen getScreen() {
        return this.screen;
    }
    
    public static class SpriteCell extends Widget {
        
        @Nullable
        private final Identifier texture;
        @Nullable
        private final ITextureAtlasSprite sprite;
        
        public SpriteCell(@Nullable Identifier texture) {
            this.texture = texture;
            if (texture == null) {
                this.sprite = null;
            } else {
                var mcSprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
                this.sprite = ITextureAtlasSprite.cast(mcSprite);
            }
            this.inlineStyle("""
                    size: 40rpx 40rpx;
                    flex-shrink: 0;
                    """);
        }
        
        @Override
        public void doRender(IGUIGraphics graphics, int mouseX, int mouseY, float a) {
            super.doRender(graphics, mouseX, mouseY, a);
            if (this.isHovered()) {
                graphics.renderOutline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), -1);
            }
            if (this.sprite != null) {
                var offset = (this.getWidth() - SPRITE_SIZE) / 2f;
                graphics.blitSprite(XKLib.RENDER_CONTEXT.get().getPipelineSource().getGuiTextured(), this.sprite, this.getX() + offset, this.getY() + offset, SPRITE_SIZE, SPRITE_SIZE);
            }
        }
        
        @Override
        protected boolean onMouseClicked(IMouseButtonEvent event, boolean doubleClick) {
            if (this.texture == null) {
                Minecraft.getInstance().keyboardHandler.setClipboard("");
            } else {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.texture.toString());
            }
            return true;
        }
        
        @Override
        public void init() {
            super.init();
            if (this.texture != null) {
                this.withTooltip(com.xkball.xklib.ui.render.IComponent.literal(this.texture.toString()));
            } else {
                this.withTooltip(com.xkball.xklib.ui.render.IComponent.literal(""));
            }
        }
    }
}
