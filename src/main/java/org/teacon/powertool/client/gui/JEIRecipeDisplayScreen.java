package org.teacon.powertool.client.gui;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.library.plugins.jei.tags.ITagInfoRecipe;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.entity.JEIRecipeDisplayBlockEntity;
import org.teacon.powertool.compat.jei.PowerToolJEIPlugin;
import org.teacon.powertool.menu.JEIRecipeDisplayMenu;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class JEIRecipeDisplayScreen extends AbstractContainerScreen<JEIRecipeDisplayMenu> {
    
    @Nullable
    private IRecipeLayoutDrawable<?> recipeLayout;
    @Nullable
    private Identifier recipeType;
    @Nullable
    private Identifier recipeId;

    public JEIRecipeDisplayScreen(JEIRecipeDisplayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        if (this.minecraft.level != null) {
            var be = this.minecraft.level.getBlockEntity(this.menu.getBlockPos());
            if (be instanceof JEIRecipeDisplayBlockEntity jeiBE) {
                this.recipeId = jeiBE.recipeId;
                this.recipeType = jeiBE.recipeType;
            }
        }
        this.updateRecipeLayout();
    }
    
    private void updateRecipeLayout(){
        this.recipeLayout = updateRecipeLayout(this.recipeType, this.recipeId);
        if (this.recipeLayout != null) {
            this.recipeLayout.setPosition(this.leftPos + 4, this.topPos + 4);
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
        if (this.recipeLayout != null) {
            this.recipeLayout.drawRecipe(graphics, mouseX, mouseY);
            this.recipeLayout.drawOverlays(graphics, mouseX, mouseY);
        }
    }
    
    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.minecraft.level != null) {
            var be = this.minecraft.level.getBlockEntity(this.menu.getBlockPos());
            if (be instanceof JEIRecipeDisplayBlockEntity jeiBE) {
                if(!Objects.equals(jeiBE.recipeId, this.recipeId) && !Objects.equals(jeiBE.recipeType, this.recipeType)) {
                    this.recipeId =  jeiBE.recipeId;
                    this.recipeType = jeiBE.recipeType;
                    this.updateRecipeLayout();
                }
            }
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
        var recipe = recipeLookup.get().filter(recipe_ -> Objects.equals(getRecipeId(recipe_), recipeId)).findFirst().orElse(null);
        if (recipe == null)  return null;
        return (IRecipeLayoutDrawable<?>) recipeManager.createRecipeLayoutDrawable(category, recipe, focusGroup).orElse(null);
    }
    
    public static @Nullable Identifier getRecipeId(Object recipe) {
        return switch (recipe) {
            case RecipeHolder<?> holder -> holder.id().identifier();
            case IJeiBrewingRecipe brewingRecipe -> brewingRecipe.getUid();
            case ITagInfoRecipe tagInfoRecipe -> tagInfoRecipe.getTag().location();
            case IJeiAnvilRecipe anvilRecipe -> anvilRecipe.getUid();
            case IJeiCompostingRecipe compostingRecipe -> compostingRecipe.getUid();
            case IJeiGrindstoneRecipe grindstoneRecipe -> grindstoneRecipe.getUid();
            
            default -> null;
        };
    }
}
