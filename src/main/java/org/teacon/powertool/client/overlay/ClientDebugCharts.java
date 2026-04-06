package org.teacon.powertool.client.overlay;

import com.mojang.datafixers.util.Pair;
import net.minecraft.SharedConstants;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.SampleLogger;

import java.util.HashMap;
import java.util.Map;

public class ClientDebugCharts {
    
    public static final Map<String, Pair<AdaptiveDebugChart, SampleLogger>> DEBUG_CHARTS = new HashMap<>();
    
    public static void recordDebugData(String id, long data) {
        if (!SharedConstants.IS_RUNNING_WITH_JDWP) return;
        var pair = DEBUG_CHARTS.computeIfAbsent(id, (k) -> {
            var logger = new LocalSampleLogger(1);
            return Pair.of(new AdaptiveDebugChart(id, logger), logger);
        });
        pair.getSecond().logSample(data);
    }
}
