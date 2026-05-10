package org.teacon.powertool.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.teacon.powertool.CreativeNoClip;

@Mixin(Player.class)
public abstract class PlayerCreativeNoClipMixin {
    
    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z")
    )
    private boolean tickTreatsCreativeNoClipAsSpectator(Player player) {
        return CreativeNoClip.canNoClip(player);
    }
    
    @Redirect(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z")
    )
    private boolean aiStepTreatsCreativeNoClipAsSpectator(Player player) {
        return CreativeNoClip.canNoClip(player);
    }
    
    @Redirect(
            method = "updatePlayerPose",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z")
    )
    private boolean poseTreatsCreativeNoClipAsSpectator(Player player) {
        return CreativeNoClip.canNoClip(player);
    }
}
