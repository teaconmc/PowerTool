package org.teacon.powertool.menu;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.teacon.powertool.block.PowerSupplyBlock;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PowerSupplyMenu extends AbstractContainerMenu {

    public PowerSupplyBlock.Data dataHolder;

    public PowerSupplyMenu(int containerId, Inventory playerInv, PowerSupplyBlock.Data dataHolder) {
        super(PowerToolMenus.POWER_SUPPLY_MENU.get(), containerId);
        this.dataHolder = dataHolder;
    }

    @Override
    public ItemStack quickMoveStack(Player p, int slot) { // TODO How?
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public record Provider(PowerSupplyBlock.Data data) implements MenuProvider {

        @Override
        public Component getDisplayName() {
            return Component.empty();
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player player) {
            return new PowerSupplyMenu(containerId, inv, this.data);
        }
    }
}
