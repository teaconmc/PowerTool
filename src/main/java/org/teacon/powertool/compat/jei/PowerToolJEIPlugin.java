package org.teacon.powertool.compat.jei;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Constants;
import net.minecraft.resources.Identifier;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PowerToolJEIPlugin implements IModPlugin {
    
    @Nullable
    public static IJeiRuntime runtime;
    
    @Override
    public Identifier getPluginUid() {
        return VanillaUtils.modRL("jei_plugin");
    }
    
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new JEIRecipeTransferHandler(), (IRecipeType<Object>) Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE);
    }
}
