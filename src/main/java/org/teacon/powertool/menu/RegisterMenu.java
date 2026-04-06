package org.teacon.powertool.menu;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegisterMenu extends AbstractContainerMenu {
    
    public record Provider(Container registerViewInv, BlockPos pos) implements MenuProvider {
        
        @Override
        public @NonNull Component getDisplayName() {
            return Component.literal("收银台");
        }
        
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inv, @NonNull Player player) {
            return new RegisterMenu(containerId, inv, this.registerViewInv, pos);
        }
    }
    
    public final BlockPos pos;
    
    protected RegisterMenu(int containerId, Inventory playerInventory, Container registerInv, BlockPos pos) {
        super(PowerToolMenus.REGISTER_MENU.get(), containerId);
        this.pos = pos;
        this.addSlot(new FakeSlot(registerInv, 0, 41, 35 - 10));
        this.addSlot(new FakeSlot(registerInv, 1, 41, 45));
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
    public @NonNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return player.getAbilities().instabuild;
    }
    
    public static class FakeSlot extends Slot {
        
        public FakeSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }
        
        @Override
        public ItemStack safeInsert(ItemStack stack, int increment) {
            this.setByPlayer(stack.copy());
            return stack;
        }
    }
}
