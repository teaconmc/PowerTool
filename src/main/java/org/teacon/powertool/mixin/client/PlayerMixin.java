package org.teacon.powertool.mixin.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.network.attachment.Permission;

import java.util.Optional;

@Mixin(Player.class)
public class PlayerMixin {

    @Shadow @Final private Abilities abilities;

    @Inject(method = "canUseGameMasterBlocks", cancellable = true, at = @At("RETURN"))
    private void usePermission(CallbackInfoReturnable<Boolean> cir) {
        // noinspection ConstantConditions
        if ((Object) this instanceof AbstractClientPlayer player) {
            cir.setReturnValue(this.abilities.instabuild &&
                    Optional.of(player.getData(PowerToolAttachments.PERMISSION))
                            .flatMap(Permission::isCanUseGameMasterBlock)
                            .orElse(player.hasPermissions(2)));
        }
    }
}
