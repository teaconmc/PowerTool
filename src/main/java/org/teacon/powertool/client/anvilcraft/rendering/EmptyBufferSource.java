package org.teacon.powertool.client.anvilcraft.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

public class EmptyBufferSource extends MultiBufferSource.BufferSource {

    public static final EmptyBufferSource INSTANCE = new EmptyBufferSource();

    protected EmptyBufferSource() {
        super(null, null);
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return EmptyVC.INSTANCE;
    }

    @Override
    public void endBatch() {

    }

    @Override
    public void endBatch(RenderType type) {

    }

    @Override
    public void endLastBatch() {

    }
}
