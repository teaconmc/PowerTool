package org.teacon.powertool.mixin.client;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {

    /**
     * @author 3TUSK
     * @reason Experimental feature: incapacitate vanilla recipe book to save up server-joining time
     */
    @Overwrite
    public void setupCollections(Iterable<RecipeHolder<?>> recipes, RegistryAccess registryAccess) {
        // Stop the recipe book from building.
    }
}
