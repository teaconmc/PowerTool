package org.teacon.powertool.mixin.client;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SectionRenderDispatcher.class)
public class SectionRenderDispatcherMixin {

    @Unique
    private static final Logger powerTool$LOGGER = LoggerFactory.getLogger("NamelessBugHunter");
    @Unique
    private static final Marker powerTool$MARKER = MarkerFactory.getMarker("SectionRenderDispatcher");

    /**
     * Tracing the exception thrown when batching all sections.
     * It is not a @Inject mixin because of a non-public class.
     * @param t The exception
     * @return The exception verbatim; we are only tracing it, not modifying it
     */
    @ModifyArg(method = "lambda$runTask$3", at = @At(value = "INVOKE", target = "Lnet/minecraft/CrashReport;forThrowable(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/CrashReport;"))
    private Throwable tracingStackTrace(Throwable t) {
        powerTool$LOGGER.error(powerTool$MARKER, "SectionRenderDispatcher encounters error!", t);
        return t;
    }


}
