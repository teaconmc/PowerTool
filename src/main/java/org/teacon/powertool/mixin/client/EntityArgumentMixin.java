package org.teacon.powertool.mixin.client;

import net.minecraft.commands.arguments.EntityArgument;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityArgument.class)
public class EntityArgumentMixin {

    /*@Redirect(method = "listSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/SharedSuggestionProvider;hasPermission(I)Z"))
    private boolean usePermission(SharedSuggestionProvider instance, int i) {
        if (instance instanceof ClientSuggestionProvider && Minecraft.getInstance().player != null) {
            return Minecraft.getInstance().player.getCapability(Permission.CAPABILITY).resolve()
                .flatMap(Permission::isCanUseSelector).orElseGet(() -> instance.hasPermission(i));
        } else {
            return instance.hasPermission(i);
        }
    }*/ // Superseded by https://github.com/MinecraftForge/MinecraftForge/pull/8947
}
