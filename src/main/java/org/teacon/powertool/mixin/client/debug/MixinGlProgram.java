package org.teacon.powertool.mixin.client.debug;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlProgram;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlProgram.class)
public class MixinGlProgram {
    
    @Shadow
    @Final
    private String debugLabel;
    
    @Shadow
    @Final
    private static Logger LOGGER;
    
    @WrapOperation(method = "setupUniforms", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL31;glGetUniformBlockIndex(ILjava/lang/CharSequence;)I"))
    public int onSetupUniform(int program, CharSequence uniformBlockName, Operation<Integer> original){
        var result = original.call(program, uniformBlockName);
        if(result == -1){
            LOGGER.warn("Cannot find Uniform block {} index in shader {}", uniformBlockName, this.debugLabel);
        }
        return result;
    }
}
