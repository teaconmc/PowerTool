package org.teacon.powertool.menu;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExhibitionEntityEditorMenu extends AbstractContainerMenu {

    protected ExhibitionEntityEditorMenu(
            final int                       windowId,
            final Inventory                 inv
    ) {
        super(PowerToolMenus.EXHIBITION_ENTITY_EDITOR_MENU.get(), windowId);
    }

    @Override
    public ItemStack quickMoveStack(
            final Player    player,
            final int       index
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return player.isAlive();
    }

    public record Provider() implements MenuProvider {

        @Override
        public Component getDisplayName() {
            return Component.literal("Exhibition Entity Editor");
        }

        @Override
        public AbstractContainerMenu createMenu(
                final int                       windowId,
                final Inventory                 inv,
                final Player                    player
        ) {
            return new ExhibitionEntityEditorMenu(windowId, inv);
        }

    }
}
