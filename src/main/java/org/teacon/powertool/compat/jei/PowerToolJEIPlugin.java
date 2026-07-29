package org.teacon.powertool.compat.jei;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Constants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.client.gui.CommandRuneLabelSelectScreen;
import org.teacon.powertool.utils.VanillaUtils;

@JeiPlugin
@MethodsReturnNonnullByDefault
@NonNullByDefault
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

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(CommandRuneLabelSelectScreen.class, screen -> new GuiProperties(CommandRuneLabelSelectScreen.class, screen.width / 2 - 80, screen.slotY() - 30, 160, 100, screen.width, screen.height));
        registration.addGhostIngredientHandler(CommandRuneLabelSelectScreen.class, new CommandRuneLabelGhostIngredientHandler());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new JEIRecipeTransferHandler(), (IRecipeType<Object>) Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE);
    }

    private record GuiProperties(
            Class<? extends Screen> screenClass, int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight
    ) implements IGuiProperties {
    }
}
