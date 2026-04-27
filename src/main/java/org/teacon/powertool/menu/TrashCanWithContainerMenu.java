package org.teacon.powertool.menu;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TrashCanWithContainerMenu extends AbstractContainerMenu {
    
    private final Container container;
    
    public TrashCanWithContainerMenu(int containerId, Inventory playerInventory, Container container) {
        super(PowerToolMenus.TRASH_CAN_MENU.get(), containerId);
        this.container = container;
        this.addSlot(new Slot(container, 0, 80, 36) {
            @Override
            public ItemStack safeInsert(ItemStack stack, int increment) {
                this.setByPlayer(stack.copy());
                return ItemStack.EMPTY;
            }
        });
        this.addStandardInventorySlots(playerInventory, 8, 84);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        var itemInSlot = slot.getItem();
        if (itemInSlot.isEmpty()) return ItemStack.EMPTY;
        if (index == 0) {
            if (!this.moveItemStackTo(itemInSlot, 1, 37, true)) return ItemStack.EMPTY;
        } else {
            this.slots.getFirst().setByPlayer(itemInSlot.copy());
            this.slots.getFirst().setChanged();
            slot.setByPlayer(ItemStack.EMPTY);
            slot.setChanged();
        }
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }
    
    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        if (slotIndex == 0 && containerInput == ContainerInput.PICKUP && (buttonNum == 0 || buttonNum == 1) && !this.getCarried().isEmpty()) {
            this.slots.getFirst().set(this.getCarried());
            this.setCarried(ItemStack.EMPTY);
        }
        else {
            super.clicked(slotIndex, buttonNum, containerInput, player);
        }
    }
    
}
