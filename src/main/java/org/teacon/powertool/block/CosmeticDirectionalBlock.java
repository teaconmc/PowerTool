package org.teacon.powertool.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public abstract class CosmeticDirectionalBlock extends CosmeticSimpleBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public CosmeticDirectionalBlock(Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getClickedFace());
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }
}
