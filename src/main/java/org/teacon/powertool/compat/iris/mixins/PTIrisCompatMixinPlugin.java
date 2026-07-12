package org.teacon.powertool.compat.iris.mixins;

import com.google.common.collect.ImmutableMap;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class PTIrisCompatMixinPlugin implements IMixinConfigPlugin {

    private boolean irisPresent = false;

    @Override
    public void onLoad(String mixinPackage) {
        irisPresent = isPresent("iris");
    }

    private boolean isPresent(String modid) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(modid) != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return irisPresent;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
