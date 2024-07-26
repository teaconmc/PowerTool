package org.teacon.powertool;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.teacon.powertool.motd.MotDHandler;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = PowerTool.MODID)
public class PowerToolConfig {

    public static ModConfigSpec.ConfigValue<String> motdContent;

    public static void init(ModContainer container) {
        var builder = new ModConfigSpec.Builder();
        motdContent = builder
                .comment("Message-of-the-day content.")
                .define("motd","");
        container.registerConfig(ModConfig.Type.SERVER, builder.build());
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        var motdText = motdContent.get();
        if (!motdText.isEmpty()) {
            MotDHandler.motd = CommonHooks.newChatWithLinks(motdContent.get());
        }
    }
}
