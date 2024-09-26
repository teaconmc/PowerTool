package org.teacon.powertool.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.PowerSupplyBlock;

public class PowerToolMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, PowerTool.MODID);

    public static DeferredHolder<MenuType<?>,MenuType<PowerSupplyMenu>> POWER_SUPPLY_MENU;
    
    public static DeferredHolder<MenuType<?>,MenuType<TrashCanWithContainerMenu>> TRASH_CAN_MENU;

    public static DeferredHolder<MenuType<?>, MenuType<RegisterMenu>> REGISTER_MENU;

    public static void register(IEventBus bus) {
        MENUS.register(bus);
        POWER_SUPPLY_MENU = MENUS.register("power_supply", () -> IMenuTypeExtension.create(((windowId, inv, data) -> {
            final var dataHolder = new PowerSupplyBlock.Data();
            dataHolder.status = data.readVarInt();
            dataHolder.power = data.readVarInt();
            return new PowerSupplyMenu(windowId, inv, dataHolder);
        })));
        TRASH_CAN_MENU = MENUS.register("trash_can_with_container",() -> IMenuTypeExtension.create(((windowId, inv, data) -> new TrashCanWithContainerMenu(windowId,inv,new SimpleContainer(1)))));
        REGISTER_MENU = MENUS.register("register", () -> IMenuTypeExtension.create(((windowId, inv, data) -> new RegisterMenu(windowId, inv, new SimpleContainer(1)))));
    }
}
