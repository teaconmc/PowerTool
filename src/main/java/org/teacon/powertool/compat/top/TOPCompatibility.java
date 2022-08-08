package org.teacon.powertool.compat.top;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import org.teacon.powertool.PowerTool;

@Mod.EventBusSubscriber(modid = PowerTool.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TOPCompatibility {

    @SubscribeEvent
    public static void onIMC(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", PowerToolTOPProvider::new);
        }
    }
}
