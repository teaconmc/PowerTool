package org.teacon.powertool.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.teacon.powertool.PowerTool;


@EventBusSubscriber(modid = DataGenerators.MOD_ID)
public class DataGenerators {
    
    public static final String MOD_ID = PowerTool.MODID;
    
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        generator.addProvider(true, new ModBlockModelProvider(output));
        generator.addProvider(true, new PowerToolBlockTagsProvider(output, lookupProvider));
        generator.addProvider(true, new PowerToolItemTagsProvider(output, lookupProvider));
    }
    
}
