package org.teacon.powertool.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.teacon.powertool.network.attachment.Permission;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackMixin {

    @WrapWithCondition(method = "broadcastToAdmins", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"))
    private boolean checkForPermission(ServerPlayer player, Component message) {
        return PermissionAPI.getPermission(player, Permission.Provider.SEE_COMMAND_FEEDBACK_FROM_OTHERS);
    }
}
