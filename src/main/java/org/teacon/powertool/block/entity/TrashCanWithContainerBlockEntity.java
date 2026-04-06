package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.menu.TrashCanWithContainerMenu;

@NonNullByDefault
public class TrashCanWithContainerBlockEntity extends BaseContainerBlockEntity {
    
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private boolean init = false;
    
    public TrashCanWithContainerBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.TRASH_CAN_WITH_CONTAINER_BLOCK_ENTITY.get(), pos, blockState);
    }
    
    public void setItem(ItemStack stack) {
        items.set(0, stack);
        this.setChanged();
    }
    
    @Override
    protected Component getDefaultName() {
        return Component.translatable("powertool.trash_can_with_container.name");
    }
    
    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }
    
    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }
    
    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new TrashCanWithContainerMenu(containerId, inventory, this);
    }
    
    @Override
    public int getContainerSize() {
        return 1;
    }
    
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
        if (!this.init) {
            items.clear();
            init = true;
        }
    }
    
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }
    
    @Override
    public void setChanged() {
        super.setChanged();
    }
}
