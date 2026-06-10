package org.teacon.powertool.compat.jei;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
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
}
