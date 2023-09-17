package org.teacon.powertool.block;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.entity.ItemDisplayBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemDisplayBlock extends BaseEntityBlock {

    protected static final VoxelShape DOWN_AABB = Block.box(2, 15, 2, 14, 16, 14);
    protected static final VoxelShape UP_AABB = Block.box(2, 0, 2, 14, 1, 14);
    protected static final VoxelShape SOUTH_AABB = Block.box(2, 2, 0, 14, 14, 1);
    protected static final VoxelShape WEST_AABB = Block.box(15, 2, 2, 16, 14, 14);
    protected static final VoxelShape NORTH_AABB = Block.box(2, 2, 15, 14, 14, 16);
    protected static final VoxelShape EAST_AABB = Block.box(0, 2, 2, 1, 14, 14);

    private static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");
    private static final BooleanProperty SURVIVAL_AVAILABLE = BooleanProperty.create("survival_available");

    public ItemDisplayBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.defaultBlockState().setValue(INVISIBLE, Boolean.FALSE).setValue(SURVIVAL_AVAILABLE,Boolean.FALSE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.powertool.item_display.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_AABB;
            case EAST -> EAST_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case UP -> UP_AABB;
            case DOWN -> DOWN_AABB;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, INVISIBLE, SURVIVAL_AVAILABLE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        return this.defaultBlockState().setValue(INVISIBLE, Boolean.FALSE).setValue(FACING, direction).setValue(SURVIVAL_AVAILABLE,Boolean.FALSE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemDisplayBlockEntity(pos, state);
    }
    @Override
    @SuppressWarnings("deprecation")
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof ItemDisplayBlockEntity theBE) {
            theBE.itemToDisplay = ItemStack.EMPTY;
            theBE.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ItemDisplayBlockEntity theBE) {
            if (player.getAbilities().instabuild || state.getValue(SURVIVAL_AVAILABLE)) {
                if (theBE.itemToDisplay.isEmpty()) {
                    theBE.itemToDisplay = player.getItemInHand(hand).copy();
                    if (!level.isClientSide) {
                        theBE.setChanged();
                        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                    }
                } else {
                    theBE.rotation = (theBE.rotation + 45) % 360;
                    theBE.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ItemDisplayBlockEntity theBE) {
            return theBE.itemToDisplay.isEmpty() ? 0 : theBE.rotation / 45 + 1;
        } else {
            return 0;
        }
    }
}
