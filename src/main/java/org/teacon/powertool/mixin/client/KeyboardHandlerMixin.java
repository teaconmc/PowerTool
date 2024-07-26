package org.teacon.powertool.mixin.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.network.attachment.Permission;

import java.util.Optional;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Redirect(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z"),
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyboardHandler;copyRecreateCommand(ZZ)V")))
    private boolean usePermission(LocalPlayer player, int i) {
        return Optional.of(player.getData(PowerToolAttachments.PERMISSION))
                .flatMap(Permission::isCanSwitchGameMode)
                .orElse(player.hasPermissions(i));
    }
}
