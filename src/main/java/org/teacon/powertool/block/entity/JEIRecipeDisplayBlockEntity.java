package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.menu.JEIRecipeDisplayMenu;

@NonNullByDefault
public class JEIRecipeDisplayBlockEntity extends BlockEntity implements MenuProvider {

    public JEIRecipeDisplayBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.JEI_RECIPE_DISPLAY_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.powertool.jei_recipe_display_block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new JEIRecipeDisplayMenu(containerId, inventory);
    }
}
