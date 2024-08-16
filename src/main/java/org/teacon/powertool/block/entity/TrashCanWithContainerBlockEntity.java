package org.teacon.powertool.block.entity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.menu.TrashCanWithContainerMenu;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TrashCanWithContainerBlockEntity extends BaseContainerBlockEntity {
    
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    
    public TrashCanWithContainerBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.TRASH_CAN_WITH_CONTAINER_BLOCK_ENTITY.get(), pos, blockState);
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
        return new TrashCanWithContainerMenu(containerId, inventory,this);
    }
    
    @Override
    public int getContainerSize() {
        return 1;
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }
    
    @Override
    public void setChanged() {
        super.setChanged();
    }
}
