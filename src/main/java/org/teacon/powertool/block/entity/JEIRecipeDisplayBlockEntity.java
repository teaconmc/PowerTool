package org.teacon.powertool.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.menu.JEIRecipeDisplayMenu;

@NonNullByDefault
public class JEIRecipeDisplayBlockEntity extends BlockEntity implements MenuProvider, IClientUpdateBlockEntity {

    @Nullable
    public Identifier recipeType;
    @Nullable
    public Identifier recipeId;
    public int yRotation;

    public JEIRecipeDisplayBlockEntity(BlockPos pos, BlockState blockState) {
        super(PowerToolBlocks.JEI_RECIPE_DISPLAY_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.recipeType = input.read("recipeType", Identifier.CODEC).orElse(null);
        this.recipeId = input.read("recipeId", Identifier.CODEC).orElse(null);
        this.yRotation = input.getIntOr("yRotation", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.recipeId != null) {
            output.store("recipeId", Identifier.CODEC, this.recipeId);
        }
        if (this.recipeType != null) {
            output.store("recipeType", Identifier.CODEC, this.recipeType);
        }
        output.putInt("yRotation", this.yRotation);
    }

    @Override
    public void writeFromClient(ValueOutput output) {
        if (this.recipeId != null) {
            output.store("recipeId", Identifier.CODEC, this.recipeId);
        }
        if (this.recipeType != null) {
            output.store("recipeType", Identifier.CODEC, this.recipeType);
        }
        output.putInt("yRotation", this.yRotation);
    }

    @Override
    public void updateFromClient(ValueInput input) {
        this.recipeType = input.read("recipeType", Identifier.CODEC).orElse(null);
        this.recipeId = input.read("recipeId", Identifier.CODEC).orElse(null);
        this.yRotation = input.getIntOr("yRotation", this.yRotation);
        if (this.level != null) {
            this.setChanged();
            var state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
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
        return new JEIRecipeDisplayMenu(containerId, inventory, this.worldPosition);
    }
}
