package org.teacon.powertool.compat.jei;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.common.Constants;
import mezz.jei.library.plugins.jei.tags.ITagInfoRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.JEIRecipeDisplayBlockEntity;
import org.teacon.powertool.client.gui.JEIRecipeDisplayScreen;
import org.teacon.powertool.menu.JEIRecipeDisplayMenu;
import org.teacon.powertool.menu.PowerToolMenus;
import org.teacon.powertool.network.server.UpdateBlockEntityData;

import java.util.Optional;

@NonNullByDefault
public class JEIRecipeTransferHandler implements IRecipeTransferHandler<JEIRecipeDisplayMenu,Object> {
    
    @Override
    public Class<? extends JEIRecipeDisplayMenu> getContainerClass() {
        return JEIRecipeDisplayMenu.class;
    }

    @Override
    public Optional<MenuType<JEIRecipeDisplayMenu>> getMenuType() {
        return Optional.of(PowerToolMenus.JEI_RECIPE_DISPLAY_MENU.get());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public IRecipeType<Object> getRecipeType() {
        return (IRecipeType<Object>) Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void customTransfer(JEIRecipeDisplayMenu container, Player player, IRecipeLayoutDrawable<?> recipeLayout){
        var typeId = recipeLayout.getRecipeCategory().getRecipeType().getUid();
        var level = player.level();
        var pos = container.getBlockPos();
        var recipe = recipeLayout.getRecipe();
        var recipeId = ((IRecipeCategory)recipeLayout.getRecipeCategory()).getIdentifier(recipe);
        if (level.getBlockEntity(pos) instanceof JEIRecipeDisplayBlockEntity be) {
            be.recipeId = recipeId;
            be.recipeType = typeId;
            ClientPacketDistributor.sendToServer(UpdateBlockEntityData.create(be));
        }
    }
    
    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(JEIRecipeDisplayMenu container, Object recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        return null;
    }
}
