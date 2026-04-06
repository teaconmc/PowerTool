package org.teacon.powertool.mixin.client;

import net.minecraft.client.ClientRecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//Experimental feature: incapacitate vanilla recipe book to save up server-joining time
@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {

//    @ModifyVariable(method = "rebuildCollections", at = @At("HEAD"), argsOnly = true)
//    public Iterable<RecipeHolder<?>> filterRecipes(Iterable<RecipeHolder<?>> recipes) {
//        return Streams.stream(recipes).filter(rep -> PowerToolConfig.recipeBookWhiteList.contains(rep.id().identifier().getNamespace())).toList();
//    }
    
    //客户端配方书现在使用一种神秘的数字id类似物来记录, 拿不到配方的rl, 暂时无法实现modid白名单 [xkball]
    @Inject(method = "rebuildCollections", at = @At("HEAD"), cancellable = true)
    public void onRebuild(CallbackInfo ci) {
        ci.cancel();
    }
}
