package org.teacon.powertool.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.teacon.powertool.PowerTool;


@EventBusSubscriber(modid = DataGenerators.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    public static final String MOD_ID = PowerTool.MODID;

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper fh = event.getExistingFileHelper();
        generator.addProvider(event.includeClient(), new ModBlockModelProvider(output, fh));
    }

}
