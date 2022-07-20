package org.teacon.powertool.mixin;

import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teacon.powertool.network.capability.Permission;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(method = "sendCommands", at = @At("RETURN"))
    private void sendPermission(ServerPlayer player, CallbackInfo ci) {
        Permission.updatePermission(player);
    }
}
