package org.teacon.powertool.mixin.client;

import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.network.attachment.Permission;

import java.util.Optional;

@Mixin(GameModeSwitcherScreen.class)
public class GameModeSwitcherScreenMixin {

    @Redirect(method = "switchToHoveredGameMode(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z"))
    private static boolean usePermission(LocalPlayer player, int i) {
        return Optional.of(player.getData(PowerToolAttachments.PERMISSION))
                .flatMap(Permission::isCanSwitchGameMode)
                .orElse(player.hasPermissions(i));
    }
}
