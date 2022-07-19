package org.teacon.powertool.mixin.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.teacon.powertool.network.capability.Permission;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Redirect(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z"),
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;copyRecreateCommand(ZZ)V")))
    private boolean usePermission(LocalPlayer player, int i) {
        return player.getCapability(Permission.CAPABILITY).resolve().flatMap(Permission::isCanSwitchGameMode).orElse(player.hasPermissions(i));
    }
}
