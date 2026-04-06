package org.teacon.powertool.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.PowerToolConfig;

@Mixin(Entity.class)
public abstract class MixinEntity {
    
    @Shadow
    public abstract Level level();
    
    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    public void onTP(TeleportTransition transition, CallbackInfoReturnable<Entity> cir) {
        var level = transition.newLevel();
        if (level.dimension() != this.level().dimension() && level.dimension() == ServerLevel.END && PowerToolConfig.disableTeleportToEnd.get()) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}
