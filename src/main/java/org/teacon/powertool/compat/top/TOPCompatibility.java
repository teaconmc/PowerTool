package org.teacon.powertool.compat.top;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import org.teacon.powertool.PowerTool;

@EventBusSubscriber(modid = PowerTool.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class TOPCompatibility {

    @SubscribeEvent
    public static void onIMC(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", PowerToolTOPProvider::new);
        }
    }
}
