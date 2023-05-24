package org.teacon.powertool.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.PowerSupplyBlock;

public class PowerToolMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, PowerTool.MODID);

    public static RegistryObject<MenuType<PowerSupplyMenu>> POWER_SUPPLY_MENU;

    public static void register() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        MENUS.register(bus);
        POWER_SUPPLY_MENU = MENUS.register("power_supply", () -> IForgeMenuType.create(((windowId, inv, data) -> {
            final var dataHolder = new PowerSupplyBlock.Data();
            dataHolder.status = data.readVarInt();
            dataHolder.power = data.readVarInt();
            return new PowerSupplyMenu(windowId, inv, dataHolder);
        })));
    }
}
