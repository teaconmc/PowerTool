package org.teacon.powertool.mixin.client;

import com.google.common.collect.Streams;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.teacon.powertool.PowerToolConfig;

//Experimental feature: incapacitate vanilla recipe book to save up server-joining time
@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {
    
    @ModifyVariable(method = "setupCollections", at = @At("HEAD"), argsOnly = true)
    public Iterable<RecipeHolder<?>> filterRecipes(Iterable<RecipeHolder<?>> recipes) {
        return Streams.stream(recipes).filter(rep -> PowerToolConfig.recipeBookWhiteList.contains(rep.id().getNamespace())).toList();
    }
    
}
