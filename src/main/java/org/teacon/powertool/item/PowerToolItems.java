package org.teacon.powertool.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.PowerToolBlocks;

@MethodsReturnNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PowerTool.MODID)
public class PowerToolItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PowerTool.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PowerTool.MODID);

    public static final RegistryObject<CreativeModeTab> THE_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.powertool"))
            .icon(() -> new ItemStack(PowerToolBlocks.COMMAND_BLOCK.get()))
            .build());

    public static void register() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        ITEMS.register("useless_stick", () -> new Item(new Item.Properties()) {
            @Override
            public boolean isFoil(ItemStack stack) {
                return true;
            }
        });
        ITEMS.register("clap", () -> new ClapItem(new Item.Properties()));
        ITEMS.register("clap_but_sad", () -> new ClapItem(new Item.Properties()));
        ITEMS.register("clap_but_angry", () -> new ClapItem(new Item.Properties()));
    }

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == THE_TAB.get()) {
            for (var regObj : ITEMS.getEntries()) {
                event.accept(regObj.get());
            }
        }
    }
}
