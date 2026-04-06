package org.teacon.powertool.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.teacon.powertool.PowerToolConfig;

@Mixin(ThrownEgg.class)
public class MixinThrownEgg {
    
    @Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    public int onHit(RandomSource instance, int i) {
        if (PowerToolConfig.noLittleChicken.get()) return 4;
        return instance.nextInt(i);
    }
}
