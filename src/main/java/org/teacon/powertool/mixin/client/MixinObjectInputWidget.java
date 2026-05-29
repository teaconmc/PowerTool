package org.teacon.powertool.mixin.client;

import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import com.xkball.xklibmc.ui.widget.mc.ObjectInputBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(ObjectInputWidget.class)
public class MixinObjectInputWidget {
    
    @Shadow
    @Final
    private ObjectInputBox inputBox;
    
    @Inject(method = "<init>",at = @At("RETURN"))
    public void onInit(Predicate validator, Function parser, CallbackInfo ci){
        this.inputBox.setMaxLength(114514);
    }
}
