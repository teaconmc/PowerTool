package org.teacon.powertool.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Locale;
import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public abstract class DedicatedServerPropertiesMixin {

    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();
    @Unique
    private static final String ENV_PREFIX = "POWERTOOL_";

    @ModifyArg(
            method = "fromFile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties;<init>(Ljava/util/Properties;)V")
    )
    private static Properties applyEnvOverrides(Properties properties) {
        System.getenv().forEach((key, value) -> {
            if (key.startsWith(ENV_PREFIX)) {
                String propKey = key.substring(ENV_PREFIX.length()).replace('_', '-').toLowerCase(Locale.ROOT);
                LOGGER.info("Applying env override: {} -> {}", propKey, value);
                properties.setProperty(propKey, value);
            }
        });
        return properties;
    }
}
