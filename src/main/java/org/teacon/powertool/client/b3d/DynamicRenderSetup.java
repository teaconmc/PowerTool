package org.teacon.powertool.client.b3d;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.teacon.powertool.annotation.NonNullByDefault;

import java.util.Map;
import java.util.function.Supplier;

@NonNullByDefault
public class DynamicRenderSetup extends RenderSetup {
    
    public final Supplier<Map<String, TextureAndSampler>> dynamicTextures;
    
    public DynamicRenderSetup(RenderSetup another, Supplier<Map<String, TextureAndSampler>> dynamicTextures){
        super(
                another.pipeline,
                another.textures,
                another.useLightmap,
                another.useOverlay,
                another.layeringTransform,
                another.outputTarget,
                another.textureTransform,
                another.outlineProperty,
                another.affectsCrumbling,
                another.sortOnUpload,
                another.bufferSize
        );
        this.dynamicTextures = dynamicTextures;
    }
    
    @Override
    public Map<String, TextureAndSampler> getTextures() {
        return dynamicTextures.get();
    }
}
