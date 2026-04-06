package org.teacon.powertool.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.PowerToolConfig;

import java.util.Set;

@Mixin(Entity.class)
public abstract class MixinEntity {
    
    @Shadow
    public abstract Level level();
    
    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    public void onTP(ServerLevel level, double x, double y, double z, Set<RelativeMovement> relativeMovements, float yRot, float xRot, CallbackInfoReturnable<Boolean> cir) {
        if (level != this.level() && level.dimension() == ServerLevel.END && PowerToolConfig.disableTeleportToEnd.get()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
