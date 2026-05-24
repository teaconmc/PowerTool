package org.teacon.powertool;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.teacon.powertool.client.PowerToolScreenProviders;

@Mod(value = PowerTool.MODID, dist = Dist.CLIENT)
public class PowerToolClient {
    
    public PowerToolClient(ModContainer modContainer, IEventBus bus) {
        PowerToolScreenProviders.init();
    }
}
