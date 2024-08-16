package org.teacon.powertool;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.entity.PowerToolEntities;
import org.teacon.powertool.item.PowerToolItems;
import org.teacon.powertool.menu.PowerToolMenus;

@Mod(PowerTool.MODID)
public class PowerTool {

    public static final String MODID = "powertool";

    public PowerTool(ModContainer modContainer, IEventBus bus) {
        PowerToolBlocks.register(bus);
        PowerToolItems.register(bus);
        PowerToolMenus.register(bus);
        PowerToolSoundEvents.register(bus);
        PowerToolEntities.register(bus);
        PowerToolAttachments.register(bus);
        PowerToolConfig.init(modContainer);
    }
}
