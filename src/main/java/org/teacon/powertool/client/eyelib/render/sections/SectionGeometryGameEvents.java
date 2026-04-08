package org.teacon.powertool.client.eyelib.render.sections;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.teacon.powertool.PowerTool;

@EventBusSubscriber(modid = PowerTool.MODID, value = Dist.CLIENT)
public class SectionGeometryGameEvents {
    @SubscribeEvent
    public static void addSectionGeometry(AddSectionGeometryEvent event) {
        event.addRenderer(new SectionGeometryBlockEntityRenderDispatcher(event.getSectionOrigin().immutable()));
    }
}
