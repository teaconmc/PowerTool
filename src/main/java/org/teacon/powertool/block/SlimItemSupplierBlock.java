package org.teacon.powertool.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SlimItemSupplierBlock extends ItemSupplierBlock {

    protected static final VoxelShape DOWN_AABB = Block.box(0, 12, 0, 16, 16, 16);
    protected static final VoxelShape UP_AABB = Block.box(0, 0, 0, 16, 4, 16);
    protected static final VoxelShape SOUTH_AABB = Block.box(0, 0, 0, 16, 16, 4);
    protected static final VoxelShape WEST_AABB = Block.box(12, 0, 0, 16, 16, 16);
    protected static final VoxelShape NORTH_AABB = Block.box(0, 0, 12, 16, 16, 16);
    protected static final VoxelShape EAST_AABB = Block.box(0, 0, 0, 4, 16, 16);

    private static final DirectionProperty FACING = BlockStateProperties.FACING;
    public SlimItemSupplierBlock(Properties prop) {
        super(prop);
    }

    @Override
    @SuppressWarnings("DuplicatedCode") //The "duplicated code" in switch cannot actually extract methods.
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
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        return this.defaultBlockState().setValue(FACING, direction);
    }

}
