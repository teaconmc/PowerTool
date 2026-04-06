package org.teacon.powertool.menu;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TextureExtractorMenu extends AbstractContainerMenu {
    
    public record Provider() implements MenuProvider {
        
        @Override
        public @NonNull Component getDisplayName() {
            return Component.literal("Texture Extractor");
        }
        
        @Override
        public AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inv, @NonNull Player player) {
            return new TextureExtractorMenu(containerId, inv);
        }
    }
    
    public final Container targetContainer;
    public boolean needRefreshFilter = false;
    
    protected TextureExtractorMenu(int containerId, Inventory playerInventory) {
        super(PowerToolMenus.TEXTURE_EXTRACTOR_MENU.get(), containerId);
        targetContainer = new SimpleContainer(1) {
            @Override
            public ItemStack removeItem(int index, int count) {
                super.removeItem(index, count);
                return ItemStack.EMPTY;
            }
            
            @Override
            public ItemStack removeItemNoUpdate(int index) {
                super.removeItemNoUpdate(index);
                return ItemStack.EMPTY;
            }
            
            @Override
            public void setChanged() {
                needRefreshFilter = true;
            }
        };
        
        this.addSlot(new RegisterMenu.FakeSlot(targetContainer, 0, 41, 35));
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
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
