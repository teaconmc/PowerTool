package org.teacon.powertool.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.teacon.powertool.PowerTool;

@MethodsReturnNonnullByDefault
public class PowerToolItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PowerTool.MODID);
    public static final CreativeModeTab TAB = new CreativeModeTab("powertool") {
        @Override
        public ItemStack makeIcon() {
            return ITEMS.getEntries().iterator().next().get().getDefaultInstance();
        }
    };

    public static void register() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        ITEMS.register("useless_stick", () -> new Item(new Item.Properties().tab(TAB)) {
            @Override
            public boolean isFoil(ItemStack stack) {
                return true;
            }
        });
    }
}
