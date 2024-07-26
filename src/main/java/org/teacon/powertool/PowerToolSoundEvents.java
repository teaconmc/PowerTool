package org.teacon.powertool;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.teacon.powertool.utils.VanillaUtils;

public class PowerToolSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, PowerTool.MODID);

    public static final DeferredHolder<SoundEvent,SoundEvent> CLAP = SOUND_EVENTS.register("clap", () -> SoundEvent.createFixedRangeEvent(VanillaUtils.resourceLocationOf(PowerTool.MODID, "item.powertool.clap"), 16F));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
