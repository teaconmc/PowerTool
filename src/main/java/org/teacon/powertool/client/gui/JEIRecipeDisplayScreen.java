package org.teacon.powertool.client.gui;

import com.mojang.logging.LogUtils;
import com.xkball.xklib.ui.css.property.value.CssLengthUnit;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.render.IGUIGraphics;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.XKLibBaseContainerScreen;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import com.xkball.xklibmc.x3d.backend.b3d.B3dGuiGraphics;
import dev.vfyjxf.taffy.geometry.FloatSize;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.library.plugins.jei.tags.ITagInfoRecipe;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.teacon.powertool.block.entity.JEIRecipeDisplayBlockEntity;
import org.teacon.powertool.compat.jei.PowerToolJEIPlugin;
import org.teacon.powertool.menu.JEIRecipeDisplayMenu;
import org.teacon.powertool.network.server.UpdateBlockEntityData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class JEIRecipeDisplayScreen extends XKLibBaseContainerScreen<JEIRecipeDisplayMenu> {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @Nullable
    private IRecipeLayoutDrawable<?> recipeLayout;
    @Nullable
    private Identifier recipeType;
    @Nullable
    private Identifier recipeId;
    @Nullable
    private RecipeLayoutWidget recipeLayoutWidget;
    @Nullable
    private Label recipeTypeLabel;
    @Nullable
    private Label recipeIdLabel;
    private int yRotation;

    public JEIRecipeDisplayScreen(JEIRecipeDisplayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.readBlockEntityData();
        this.updateRecipeLayout();
        this.addScreenLayer(this.createLayout());
    }
    
    private void readBlockEntityData() {
        if (this.minecraft.level == null) {
            return;
        }
        var be = this.minecraft.level.getBlockEntity(this.menu.getBlockPos());
        if (be instanceof JEIRecipeDisplayBlockEntity jeiBE) {
            this.recipeId = jeiBE.recipeId;
            this.recipeType = jeiBE.recipeType;
            this.yRotation = jeiBE.yRotation;
        }
    }
    
    private Widget createLayout() {
        this.recipeLayoutWidget = new RecipeLayoutWidget(() -> this.recipeLayout);
        this.recipeTypeLabel = new Label(IComponent.literal(recipeTypeText()));
        this.recipeIdLabel = new Label(IComponent.literal(recipeIdText()));
        var rotationSlider = new RotationSlider(0, 0, 0, 0, this.yRotation, value -> {
            this.yRotation = value;
            this.writeRotationToBlockEntity();
        });
        var rotationSliderWrapper = new WidgetWrapper(rotationSlider);
        rotationSliderWrapper.setUserInput(true);
        return new ContainerWidget()
                .inlineStyle("""
                        flex-direction: column;
                        size: 100% 100%;
                        align-items: center;
                        justify-content: center;
                        """)
                .asRootStyle("""
                        Label {
                            text-color: -1;
                            text-height: 10rpx;
                            text-align: center;
                        }
                        .recipe_info {
                            size: 90% 16rpx;
                            flex-shrink: 0;
                            text-scale: expand-width;
                        }
                        .recipe_layout_spacer {
                            size: 100% 20rpx;
                            flex-shrink: 1;
                        }
                        .rotation_slider {
                            size: 180rpx 20rpx;
                            margin-top: 12rpx;
                            flex-shrink: 0;
                        }
                        """)
                .addChild(this.recipeTypeLabel.setCSSClassName("recipe_info"))
                .addChild(this.recipeIdLabel.setCSSClassName("recipe_info"))
                .addChild(new Widget().setCSSClassName("recipe_layout_spacer"))
                .addChild(this.recipeLayoutWidget)
                .addChild(new Widget().setCSSClassName("recipe_layout_spacer"))
                .addChild(rotationSliderWrapper.setCSSClassName("rotation_slider"));
    }
    
    private String recipeTypeText() {
        return "recipeType: " + valueText(this.recipeType);
    }
    
    private String recipeIdText() {
        return "recipeId: " + valueText(this.recipeId);
    }
    
    private static String valueText(@Nullable Identifier value) {
        return value == null ? "" : value.toString();
    }
    
    private void writeRotationToBlockEntity() {
        if (this.minecraft.level == null) {
            return;
        }
        var be = this.minecraft.level.getBlockEntity(this.menu.getBlockPos());
        if (be instanceof JEIRecipeDisplayBlockEntity jeiBE && jeiBE.yRotation != this.yRotation) {
            jeiBE.yRotation = this.yRotation;
            ClientPacketDistributor.sendToServer(UpdateBlockEntityData.create(jeiBE));
        }
    }
    
    private void updateRecipeLayout(){
        this.recipeLayout = updateRecipeLayout(this.recipeType, this.recipeId);
        if (this.recipeLayoutWidget != null) {
            this.recipeLayoutWidget.markDirty();
        }
        if (this.recipeTypeLabel != null) {
            this.recipeTypeLabel.setText(IComponent.literal(this.recipeTypeText()));
        }
        if (this.recipeIdLabel != null) {
            this.recipeIdLabel.setText(IComponent.literal(this.recipeIdText()));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
//        graphics.blit(RenderPipelines.GUI_TEXTURED, BG, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public void containerTick() {
        super.containerTick();
        if (this.minecraft.level != null) {
            var be = this.minecraft.level.getBlockEntity(this.menu.getBlockPos());
            if (be instanceof JEIRecipeDisplayBlockEntity jeiBE) {
                if(!Objects.equals(jeiBE.recipeId, this.recipeId) || !Objects.equals(jeiBE.recipeType, this.recipeType)) {
                    this.recipeId =  jeiBE.recipeId;
                    this.recipeType = jeiBE.recipeType;
                    this.updateRecipeLayout();
                }
                this.yRotation = jeiBE.yRotation;
            }
        }
        if (this.recipeLayout != null) {
            this.recipeLayout.tick();
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static IRecipeLayoutDrawable<?> updateRecipeLayout(@Nullable Identifier recipeType, @Nullable Identifier recipeId) {
        if (recipeId == null || recipeType == null) {
            return null;
        }
        var runtime = PowerToolJEIPlugin.runtime;
        if(runtime == null) return null;
        var recipeManager = runtime.getRecipeManager();
        var type_ = recipeManager.getRecipeType(recipeType);
        if (type_.isEmpty()) return null;
        var type = type_.get();
        IRecipeCategory category = recipeManager.getRecipeCategory(type);
        var focusGroup = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
        var recipeLookup = recipeManager.createRecipeLookup(type);
        var recipe = recipeLookup.get().filter(recipe_ -> Objects.equals(category.getIdentifier(recipe_), recipeId)).findFirst().orElse(null);
        if (recipe == null)  return null;
        return (IRecipeLayoutDrawable<?>) recipeManager.createRecipeLayoutDrawable(category, recipe, focusGroup).orElse(null);
    }
    
    private interface IntValueConsumer {
        void accept(int value);
    }
    
    private static class RotationSlider extends AbstractSliderButton {
        
        private static final int MIN = -90;
        private static final int MAX = 90;
        
        private final IntValueConsumer consumer;
        
        public RotationSlider(int x, int y, int width, int height, int value, IntValueConsumer consumer) {
            super(x, y, width, height, Component.empty(), normalize(value));
            this.consumer = consumer;
            this.updateMessage();
        }
        
        private static double normalize(int value) {
            return (double) (Math.clamp(value, MIN, MAX) - MIN) / (MAX - MIN);
        }
        
        private int getRotation() {
            return (int) Math.round(MIN + this.value * (MAX - MIN));
        }
        
        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal("Rotation: " + this.getRotation()));
        }
        
        @Override
        protected void applyValue() {
            this.consumer.accept(this.getRotation());
        }
    }
    
    private static class RecipeLayoutWidget extends Widget {
        
        private final RecipeLayoutSupplier supplier;
        
        public RecipeLayoutWidget(RecipeLayoutSupplier supplier) {
            this.supplier = supplier;
            this.inlineStyle("size: content content; flex-shrink: 0;");
        }
        
        @Override
        public void afterTreeAndNodeSet() {
            super.afterTreeAndNodeSet();
            this.tree.setMeasureFunc(this.nodeId, (knownDimensions, availableSpace) -> {
                var layout = this.supplier.get();
                if (layout == null) {
                    return FloatSize.zero();
                }
                var rect = layout.getRect();
                var scale = CssLengthUnit.rpxScaleWorkaround;
                if (scale == 0) {
                    scale = 1;
                }
                return FloatSize.of(rect.getWidth() * scale, rect.getHeight() * scale);
            });
        }
        
        @Override
        public void doRender(IGUIGraphics graphics, int mouseX, int mouseY, float a) {
            if (!(graphics instanceof B3dGuiGraphics b3dGraphics)) {
                return;
            }
            var layout = this.supplier.get();
            if (layout == null) {
                return;
            }
            var inner = b3dGraphics.getInner();
            var scale = b3dGraphics.scale;
            if (scale == 0) {
                scale = 1;
            }
            var rect = layout.getRect();
            var layoutX = Math.round((this.getX() + this.getWidth() / 2f) / scale - rect.getWidth() / 2f);
            var layoutY = Math.round((this.getY() + this.getHeight() / 2f) / scale - rect.getHeight() / 2f);
            var layoutMouseX = Math.round(mouseX / scale);
            var layoutMouseY = Math.round(mouseY / scale);
            layout.setPosition(layoutX, layoutY);
            inner.pose().pushMatrix();
            inner.pose().scale(scale, scale);
            layout.drawRecipe(inner, layoutMouseX, layoutMouseY);
            layout.drawOverlays(inner, layoutMouseX, layoutMouseY);
            inner.pose().popMatrix();
        }
    }
    
    private interface RecipeLayoutSupplier {
        
        @Nullable
        IRecipeLayoutDrawable<?> get();
    }
}
