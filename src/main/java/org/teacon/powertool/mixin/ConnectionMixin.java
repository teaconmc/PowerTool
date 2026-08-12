package org.teacon.powertool.mixin;

import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    
    @Unique
    private static final Logger powerTool$LOGGER = LoggerFactory.getLogger("PowerTool-ConnectionMixin");
    
    @Inject(method = "handleDisconnection",at = @At("HEAD"))
    public void onDisconnect(CallbackInfo ci){
        StackWalker.getInstance().forEach(frame -> {
            powerTool$LOGGER.debug(frame.toString());
        });
    }
}
