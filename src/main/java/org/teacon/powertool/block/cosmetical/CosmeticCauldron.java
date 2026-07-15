package org.teacon.powertool.block.cosmetical;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CosmeticCauldron extends CosmeticSimpleBlock {

    private static final VoxelShape INSIDE = Block.column(12.0, 4.0, 16.0);
    private static final VoxelShape SHAPE = Shapes.join(
            Shapes.block(),
            Shapes.or(
                    Block.column(16.0, 8.0, 0.0, 3.0),
                    Block.column(8.0, 16.0, 0.0, 3.0),
                    Block.column(12.0, 0.0, 3.0),
                    INSIDE
            ),
            BooleanOp.ONLY_FIRST
    );

    public CosmeticCauldron(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return INSIDE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
