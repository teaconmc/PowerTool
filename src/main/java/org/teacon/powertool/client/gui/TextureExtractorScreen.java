package org.teacon.powertool.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.teacon.powertool.client.gui.widget.TextureAtlasSpriteList;
import org.teacon.powertool.menu.TextureExtractorMenu;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TextureExtractorScreen extends AbstractContainerScreen<TextureExtractorMenu> {
    
    private static final Identifier BG_LOCATION = VanillaUtils.modRL("textures/gui/texture_extractor.png");
    
    protected TextureAtlasSpriteList textureAtlasSpriteList;
    private EditBox searchBar;
    
    public TextureExtractorScreen(TextureExtractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
    
    @Override
    protected void init() {
        super.init();
        this.textureAtlasSpriteList = new TextureAtlasSpriteList(this, (int) (width * 0.3), (int) (height * 0.8), (int) (height * 0.1), 30);
        this.searchBar = new EditBox(font, (int) (10 + width * 0.1), (int) (height * 0.9 + 5), (int) (width * 0.2), 20, Component.empty());
        this.searchBar.setResponder((str) -> menu.needRefreshFilter = true);
        this.searchBar.setMaxLength(1000);
        this.addRenderableWidget(textureAtlasSpriteList);
        this.addRenderableWidget(searchBar);
    }
    
    @Override
    protected void containerTick() {
        super.containerTick();
        if (menu.needRefreshFilter) {
            textureAtlasSpriteList.update();
            menu.needRefreshFilter = false;
        }
    }
    
    public List<Identifier> getFilteredTextures() {
        var filteredTexturesSet = new HashSet<Identifier>();
        var mc = Minecraft.getInstance();
        var item = menu.targetContainer.getItem(0);
        if (item.isEmpty()) {
            //noinspection deprecation
            filteredTexturesSet.addAll(mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getTextures().keySet());
        } else {
            var model = mc.getItemRenderer().getModel(item, mc.level, mc.player, 943);
            var quads = model.getQuads(null, null, RandomSource.create(943), ModelData.EMPTY, null);
            filteredTexturesSet.add(model.getParticleIcon(ModelData.EMPTY).contents().name());
            filteredTexturesSet.addAll(quads.stream().map(quad -> quad.getSprite().contents().name()).toList());
        }
        return filteredTexturesSet.stream().filter(rl -> {
            if (searchBar == null) return true;
            var str = searchBar.getValue().toLowerCase();
            if (str.isEmpty()) return true;
            return rl.toString().contains(str);
        }).sorted(Comparator.comparing(Identifier::toString)).toList();
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (textureAtlasSpriteList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBar.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (searchBar.isFocused() && searchBar.isActive()) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (searchBar.keyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBar.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BG_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        var str = "Search: ";
        guiGraphics.drawString(font, str, (int) (10 + width * 0.1) - font.width(str) - 2, (int) (height * 0.9 + 5 + 2), -1);
    }
}
