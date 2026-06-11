package org.teacon.powertool.menu;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.teacon.powertool.block.entity.JEIRecipeDisplayBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class JEIRecipeDisplayMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;

    public JEIRecipeDisplayMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(PowerToolMenus.JEI_RECIPE_DISPLAY_MENU.get(), containerId);
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        var level = player.level();
        return level.getBlockEntity(blockPos) instanceof JEIRecipeDisplayBlockEntity
                && player.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) <= 64.0;
    }
}
