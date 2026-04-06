package org.teacon.powertool.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debugchart.AbstractDebugChart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.debugchart.SampleStorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdaptiveDebugChart extends AbstractDebugChart {
    
    private double avgOld = 1;
    private final String id;
    
    protected AdaptiveDebugChart(String id, SampleStorage sampleStorage) {
        super(Minecraft.getInstance().font, sampleStorage);
        this.id = id;
    }
    
    public void drawChart(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(RenderType.guiOverlay(), x, y - 60, x + width, y, -1873784752);
        long sumData = 0L;
        long absSumData = 0L;
        long minData = 2147483647L;
        long maxData = -2147483648L;
        int i1 = Math.max(0, this.sampleStorage.capacity() - (width - 2));
        int j1 = this.sampleStorage.size() - i1;
        var zeroCount = 0;
        for (int k1 = 0; k1 < j1; k1++) {
            int l1 = x + k1 + 1;
            int i2 = i1 + k1;
            long j2 = this.getValueForAggregation(i2);
            minData = Math.min(minData, j2);
            maxData = Math.max(maxData, j2);
            sumData += j2;
            absSumData += Math.abs(j2);
            if (j2 == 0) zeroCount++;
            this.drawDimensions(guiGraphics, y, l1, i2);
        }
        this.drawStringWithShade(guiGraphics, this.sampleStorage.get(i1 + j1 - 1) + " current", x + width + 1, y - 42);
        guiGraphics.hLine(RenderType.guiOverlay(), x, x + width - 1, y - 60, -1);
        guiGraphics.hLine(RenderType.guiOverlay(), x, x + width - 1, y - 1, -1);
        guiGraphics.vLine(RenderType.guiOverlay(), x, y - 60, y, -1);
        guiGraphics.vLine(RenderType.guiOverlay(), x + width - 1, y - 60, y, -1);
        if (j1 > 0) {
            avgOld = (double) absSumData / ((double) j1 - zeroCount);
            String s = this.toDisplayString((double) minData) + " min";
            String s1 = this.toDisplayString((double) sumData / (double) j1) + " avg";
            String s2 = this.toDisplayString((double) maxData) + " max";
            guiGraphics.drawString(this.font, s, x + 2, y - 60 - 9, 14737632);
            guiGraphics.drawCenteredString(this.font, s1, x + width / 2, y - 60 - 9, 14737632);
            guiGraphics.drawString(this.font, s2, x + width - this.font.width(s2) - 2, y - 60 - 9, 14737632);
        }
        
        this.renderAdditionalLinesAndLabels(guiGraphics, x, width, y);
    }
    
    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int x, int width, int height) {
        var avgHeight = height - 30 - getSampleHeight(avgOld);
        this.drawStringWithShade(guiGraphics, id, x + 1, height - 60 - font.lineHeight * 2 - 2);
        this.drawStringWithShade(guiGraphics, avgOld + " avg", x + width + 1, avgHeight);
        this.drawStringWithShade(guiGraphics, "0", x + width + 1, height - 30);
        guiGraphics.hLine(RenderType.guiOverlay(), x, x + width - 1, avgHeight, -1);
        guiGraphics.hLine(RenderType.guiOverlay(), x, x + width - 1, height - 30, -1);
    }
    
    @Override
    protected void drawMainDimension(GuiGraphics guiGraphics, int height, int x, int index) {
        long i = this.sampleStorage.get(index);
        int j = -this.getSampleHeight((double) i);
        int k = this.getSampleColor(i);
        var y1 = height - 30;
        var y2 = y1 + j;
        guiGraphics.fill(RenderType.guiOverlay(), x, Math.min(y1, y2), x + 1, Math.max(y1, y2), k);
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
