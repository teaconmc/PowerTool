package org.teacon.powertool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PowerToolSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PowerTool.MODID);

    public static final RegistryObject<SoundEvent> CLAP = SOUND_EVENTS.register("clap", () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(PowerTool.MODID, "item.powertool.clap"), 16F));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
