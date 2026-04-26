package org.teacon.powertool.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debugchart.AbstractDebugChart;
import net.minecraft.util.debugchart.SampleStorage;

public class AdaptiveDebugChart extends AbstractDebugChart {
    
    private double avgOld = 1;
    private final String id;
    
    protected AdaptiveDebugChart(String id, SampleStorage sampleStorage) {
        super(Minecraft.getInstance().font, sampleStorage);
        this.id = id;
    }
    
    @Override
    protected void extractAdditionalLinesAndLabels(GuiGraphicsExtractor graphics, int x, int width, int bottom) {
        var height = getFullHeight();
        var avgHeight = height - 30 - getSampleHeight(avgOld);
        this.extractStringWithShade(graphics, id, x + 1, height - 60 - font.lineHeight * 2 - 2);
        this.extractStringWithShade(graphics, avgOld + " avg", x + width + 1, avgHeight);
        this.extractStringWithShade(graphics, "0", x + width + 1, height - 30);
        graphics.horizontalLine(x, x + width - 1, avgHeight, -1);
        graphics.horizontalLine(x, x + width - 1, height - 30, -1);
    }
    
    @Override
    protected void extractMainSampleBar(GuiGraphicsExtractor graphics, int bottom, int x, int sampleIndex) {
        long i = this.sampleStorage.get(sampleIndex);
        int j = -this.getSampleHeight((double) i);
        int k = this.getSampleColor(i);
        var y1 = getFullHeight() - 30;
        var y2 = y1 + j;
        graphics.fill( x, Math.min(y1, y2), x + 1, Math.max(y1, y2), k);
    }
    
    @Override
    protected String toDisplayString(double value) {
        return String.valueOf(value);
    }
    
    @Override
    protected int getSampleHeight(double value) {
        var scale = value / Math.abs(avgOld);
        if (Double.isNaN(scale)) {
            return (int) value;
        }
        return (int) (scale * 24);
    }
    
    @Override
    protected int getSampleColor(long value) {
        return getSampleColor(Math.abs(getSampleHeight(value)), 0, -16711936, 30, -256, 60, -65536);
    }
}
