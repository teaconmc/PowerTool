package org.teacon.powertool.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.teacon.powertool.network.capability.Permission;

@Mixin(EntityArgument.class)
public class EntityArgumentMixin {

    @Redirect(method = "listSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/SharedSuggestionProvider;hasPermission(I)Z"))
    private boolean usePermission(SharedSuggestionProvider instance, int i) {
        if (instance instanceof ClientSuggestionProvider && Minecraft.getInstance().player != null) {
            return Minecraft.getInstance().player.getCapability(Permission.CAPABILITY).resolve()
                .flatMap(Permission::isCanUseSelector).orElseGet(() -> instance.hasPermission(i));
        } else {
            return instance.hasPermission(i);
        }
    }
}
