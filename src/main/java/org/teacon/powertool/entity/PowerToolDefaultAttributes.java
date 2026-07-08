package org.teacon.powertool.entity;

import net.minecraft.world.entity.decoration.Mannequin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.entity.exhibit.ExhibitionHumanoid;

@EventBusSubscriber(modid = PowerTool.MODID)
public class PowerToolDefaultAttributes {


    @SubscribeEvent
    public static void onCreateEntityAttributes(final EntityAttributeCreationEvent event) {

        event.put(
                PowerToolEntities.REGULAR_EXHIBITION_HUMANOID.get(),
                ExhibitionHumanoid.createAttributes().build()
        );

        event.put(
                PowerToolEntities.SLIM_EXHIBITION_HUMANOID.get(),
                ExhibitionHumanoid.createAttributes().build()
        );

    }

}
