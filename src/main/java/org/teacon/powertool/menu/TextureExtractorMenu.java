package org.teacon.powertool.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.teacon.powertool.annotation.NonNullByDefault;

@NonNullByDefault
public class TextureExtractorMenu extends AbstractContainerMenu {

    public record Provider() implements MenuProvider {

        @Override
        public Component getDisplayName() {
            return Component.translatable("item.powertool.texture_extractor");
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return new TextureExtractorMenu(containerId, inventory);
        }
    }

    public TextureExtractorMenu(int containerId, Inventory inventory) {
        super(PowerToolMenus.TEXTURE_EXTRACTOR_MENU.get(), containerId);
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
