package org.teacon.powertool;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.teacon.powertool.motd.MotDHandler;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PowerTool.MODID)
public class PowerToolConfig {

    public static ForgeConfigSpec.ConfigValue<String> motdContent;

    public static void init(ModLoadingContext context) {
        var configSpec = new ForgeConfigSpec.Builder();
        motdContent = configSpec.comment("Message-of-the-day content.").define("motd", "");
        context.registerConfig(ModConfig.Type.SERVER, configSpec.build());
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        var motdText = motdContent.get();
        if (motdText != null && !motdText.isEmpty()) {
            MotDHandler.motd = ForgeHooks.newChatWithLinks(motdContent.get());
        }
    }
}
