package org.teacon.powertool;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.motd.MotDHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = PowerTool.MODID)
@NonNullByDefault
public class PowerToolConfig {
    
    public static ModConfigSpec.ConfigValue<String> motdContent;
    
    public static ModConfigSpec.ConfigValue<Boolean> disableTeleportToEnd;
    
    public static ModConfigSpec.ConfigValue<Boolean> disableBeaconRender;
    
    public static ModConfigSpec.ConfigValue<Boolean> vehicleAutoVanish;
    
    public static ModConfigSpec.ConfigValue<Boolean> noLittleChicken;
    
    public static ModConfigSpec.ConfigValue<List<? extends String>> recipeBookWhiteListConfig;
    
    public static Set<String> recipeBookWhiteList;
    
    public static void init(ModContainer container) {
        var builder = new ModConfigSpec.Builder();
        motdContent = builder.comment("Message-of-the-day content.").define("motd", "");
        disableTeleportToEnd = builder.comment("Disable the access of the End.").define("disableTeleportToEnd", true);
        disableBeaconRender = builder.comment("Disable beacon beam rendering.").define("disableBeaconRender", true);
        vehicleAutoVanish = builder.comment("Replace boat and minecart as auto banish version when placing.(Not include chest boat.)").define("vehicleAutoVanish", true);
        noLittleChicken = builder.comment("Disable thrown egg create little chickens.").define("noLittleChicken", true);
        recipeBookWhiteListConfig = builder.comment("A list of namespace that allows build recipe book.").defineListAllowEmpty("recipeBookWhiteList", List.of("anomaly_delight"), null, (obj) -> true);
        container.registerConfig(ModConfig.Type.SERVER, builder.build());
    }
    
    public static void update() {
        var motdText = motdContent.get();
        if (!motdText.isEmpty()) {
            MotDHandler.motd = CommonHooks.newChatWithLinks(motdContent.get());
        }
        recipeBookWhiteList = new HashSet<>(recipeBookWhiteListConfig.get());
    }
    
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        update();
    }
    
    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        update();
    }
}
