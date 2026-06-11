package org.teacon.powertool.mixin.jei;

import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.transfer.RecipeTransferUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.compat.jei.JEIRecipeTransferHandler;
import org.teacon.powertool.menu.JEIRecipeDisplayMenu;

import java.util.Optional;

@Mixin(RecipeTransferUtil.class)
public class MixinRecipeTransferUtil {
    
    @Inject(method = "transferRecipe(Lmezz/jei/api/recipe/transfer/IRecipeTransferManager;Lnet/minecraft/world/inventory/AbstractContainerMenu;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/world/entity/player/Player;ZZ)Ljava/util/Optional;",
    at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/transfer/IRecipeTransferHandler;transferRecipe(Lnet/minecraft/world/inventory/AbstractContainerMenu;Ljava/lang/Object;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/world/entity/player/Player;ZZ)Lmezz/jei/api/recipe/transfer/IRecipeTransferError;"))
    private static <C extends AbstractContainerMenu, R> void onTransferRecipe(IRecipeTransferManager recipeTransferManager, C container, IRecipeLayoutDrawable<R> recipeLayout, Player player, boolean maxTransfer, boolean doTransfer, CallbackInfoReturnable<Optional<IRecipeTransferError>> cir, @Local(name = "transferHandler") IRecipeTransferHandler<C, R> transferHandler){
        if(doTransfer && transferHandler instanceof JEIRecipeTransferHandler handler && container instanceof JEIRecipeDisplayMenu menu){
            handler.customTransfer(menu, player, recipeLayout);
        }
    }
}
