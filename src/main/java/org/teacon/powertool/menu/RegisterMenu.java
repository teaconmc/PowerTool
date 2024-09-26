package org.teacon.powertool.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RegisterMenu extends AbstractContainerMenu {

    public record Provider(Container registerViewInv) implements MenuProvider {

        @Override
        public @NotNull Component getDisplayName() {
            return Component.literal("收银台");
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inv, @NotNull Player player) {
            return new RegisterMenu(containerId, inv, this.registerViewInv);
        }
    }

    protected RegisterMenu(int containerId, Inventory playerInventory, Container registerInv) {
        super(PowerToolMenus.REGISTER_MENU.get(), containerId);
        this.addSlot(new Slot(registerInv, 0, 41, 35) {
            @Override
            public ItemStack safeInsert(ItemStack stack, int increment) {
                this.setByPlayer(stack.copy());
                return stack;
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getAbilities().instabuild;
    }
}
