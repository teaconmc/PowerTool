package org.teacon.powertool.block.cosmetical;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.teacon.powertool.block.CosmeticBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CosmeticTrapdoor extends DirectionalBlock implements SimpleWaterloggedBlock, CosmeticBlock {
    
    public static final MapCodec<CosmeticTrapdoor> CODEC = simpleCodec(CosmeticTrapdoor::new);

    protected static final VoxelShape EAST_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_OPEN_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
    protected static final VoxelShape TOP_AABB = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);


    public CosmeticTrapdoor(Properties prop) {
        super(prop);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP).setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE));
    }
    
    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }
    
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.WATERLOGGED);
    }

    @Override
    @SuppressWarnings("DuplicatedCode") //The "duplicated code" in switch cannot actually extract methods.
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return switch (state.getValue(FACING)) {
            case UP -> BOTTOM_AABB;
            case DOWN -> TOP_AABB;
            case SOUTH -> SOUTH_OPEN_AABB;
            case WEST -> WEST_OPEN_AABB;
            case EAST -> EAST_OPEN_AABB;
            case NORTH -> NORTH_OPEN_AABB;
        };
    }
    
    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return pathComputationType == PathComputationType.WATER && state.getValue(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        return this.defaultBlockState().setValue(FACING, direction);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("block.powertool.cosmetic_trapdoor.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(TrapDoorBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
