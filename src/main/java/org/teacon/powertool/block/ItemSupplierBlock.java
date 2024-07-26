package org.teacon.powertool.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.teacon.powertool.block.entity.ItemSupplierBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemSupplierBlock extends BaseEntityBlock {
    
    public static final MapCodec<ItemSupplierBlock> CODEC =  simpleCodec(ItemSupplierBlock::new);
    public ItemSupplierBlock(Properties prop) {
        super(prop);
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
   
    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return adjacentBlockState.is(this) || super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("block.powertool.item_supplier.tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemSupplierBlockEntity(pos, state);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ItemSupplierBlockEntity theBE) {
            ItemStack thing = theBE.theItem.copy();
            thing.setCount(player.isCrouching() ? thing.getMaxStackSize() : 1);
            player.getInventory().add(thing);
        }
    }
    
    protected void giveItemToPlayer(Player player, ItemSupplierBlockEntity theTE) {
        ItemStack toGive = theTE.theItem.copy();
        toGive.setCount(player.isCrouching() ? toGive.getMaxStackSize() : 1);
        player.getInventory().add(toGive);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ItemSupplierBlockEntity theBE) {
            giveItemToPlayer(player, theBE);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ItemSupplierBlockEntity theBE){
            if (theBE.theItem.isEmpty() && player.getAbilities().instabuild) {
                theBE.theItem = stack.copy();
                if (!level.isClientSide) {
                    theBE.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                }
            }
            else{
                giveItemToPlayer(player, theBE);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
