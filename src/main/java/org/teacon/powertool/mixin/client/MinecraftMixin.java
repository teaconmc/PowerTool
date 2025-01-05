package org.teacon.powertool.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.client.anvilcraft.rendering.CacheableBERenderingPipeline;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(
        method = "updateLevelInEngines",
        at = @At("HEAD")
    )
    void updateLevel(ClientLevel level, CallbackInfo ci){
        CacheableBERenderingPipeline.updateLevel(level);
    }
}
