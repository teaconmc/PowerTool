package org.teacon.powertool.block.fluid;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.teacon.powertool.block.PowerToolBlocks;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FakeWater extends FlowingFluid {
    
    @Override
    public Item getBucket() {
        return PowerToolBlocks.FAKE_WATER_BUCKET.get();
    }
    
    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }
    
    @Override
    public Vec3 getFlow(BlockGetter blockReader, BlockPos pos, FluidState fluidState) {
        return Vec3.ZERO;
    }
    
    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }
    
    @Override
    protected float getExplosionResistance() {
        return 100;
    }
    
    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return PowerToolBlocks.FAKE_WATER_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }
    
    @Override
    public boolean isSource(FluidState state) {
        return true;
    }
    
    @Override
    public Fluid getFlowing() {
        return PowerToolBlocks.FAKE_WATER.get();
    }
    
    @Override
    public Fluid getSource() {
        return PowerToolBlocks.FAKE_WATER.get();
    }
    
    @Override
    protected boolean canConvertToSource(ServerLevel level) {
        return false;
    }
    
    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockentity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockentity);
    }
    
    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 0;
    }
    
    @Override
    protected void spread(ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState) {
    
    }
    
    @Override
    protected int getDropOff(LevelReader level) {
        return 8;
    }
    
    @Override
    public int getAmount(FluidState state) {
        return 8;
    }
    
    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }
    
    @Override
    public FluidType getFluidType() {
        return PowerToolBlocks.FAKE_WATER_TYPE.get();
    }
}
