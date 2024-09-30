package org.teacon.powertool.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.entity.TempleBlockEntity;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TempleBlock extends BaseEntityBlock {
    
    public static final MapCodec<TempleBlock> CODEC = simpleCodec(TempleBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    
    public static final VoxelShape[] SHAPE = new VoxelShape[]{
            Block.box(2.5D, 0.0D, 6.5D, 13.5D, 20.0D, 13.5D), //NORTH
            Block.box(6.5D,0.0D,2.5D,13.5D,20.0D,13.5D), //EAST
            Block.box(2.5D, 0.0D, 2.5D, 13.5D, 20.0D, 9.5D), //SOUTH
            Block.box(2.5D,0.0D,2.5D,9.5D,20.0D,13.5D), //WEST
    };
    
    public TempleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.isClientSide()) {
            return ItemInteractionResult.CONSUME;
        }
        if(stack.isEmpty()){
            if(player instanceof ServerPlayer sp) {
                if(player.isCrouching() && player.getAbilities().instabuild) setTheItem(ItemStack.EMPTY,level,pos,state);
                else sp.setRespawnPosition(level.dimension(),pos.relative(state.getValue(HORIZONTAL_FACING)),player.getYRot(),true,true);
            }
        }
        else if(player.getAbilities().instabuild){
            setTheItem(stack,level,pos,state);
        }
        return ItemInteractionResult.SUCCESS;
    }
    
    protected void setTheItem(ItemStack stack, Level level, BlockPos pos, BlockState state){
        var te = level.getBlockEntity(pos);
        if(te instanceof TempleBlockEntity tb){
            tb.theItem = stack.copy();
            tb.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }
    
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING,WATERLOGGED);
    }
    
    @Override
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var facing = state.getValue(HORIZONTAL_FACING);
        return SHAPE[shapeIndex(facing)];
    }
    
    protected int shapeIndex(Direction direction) {
        return switch (direction) {
            case WEST -> 1;
            case SOUTH -> 2;
            case EAST -> 3;
            default -> 0;
        };
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState()
                .setValue(WATERLOGGED, flag)
                .setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TempleBlockEntity(pos, state);
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if(level.getBlockEntity(pos) instanceof TempleBlockEntity te){
            if(te.theItem.isEmpty()) return;
            var facing = state.getValue(HORIZONTAL_FACING);
            var r = (facing.get2DDataValue()+2)%4;
            var p1 = VanillaUtils.rotate90FromBlockCenterYP(new Vec2(7.5f,-0.5f),r);
            var p2 = VanillaUtils.rotate90FromBlockCenterYP(new Vec2(8.5f,0.5f),r);
            var px = pos.getX();
            var py = pos.getY();
            var pz = pos.getZ();
            if(random.nextBoolean()) addParticlesAndSound(level,new Vec3(px+p1.x/16, py+ 0.5625f,pz+p1.y/16),random);
            if(random.nextBoolean()) addParticlesAndSound(level,new Vec3(px+p2.x/16, py+ 0.6875f,pz+p2.y/16),random);
        }
    }
    
    /**
     * @see net.minecraft.world.level.block.AbstractCandleBlock#addParticlesAndSound(Level, Vec3, RandomSource)
     */
    private static void addParticlesAndSound(Level level, Vec3 offset, RandomSource random) {
        float f = random.nextFloat();
        if (f < 0.3F) {
            level.addParticle(ParticleTypes.SMOKE, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
            if (f < 0.17F) {
                level.playLocalSound(
                        offset.x + 0.5,
                        offset.y + 0.5,
                        offset.z + 0.5,
                        SoundEvents.CANDLE_AMBIENT,
                        SoundSource.BLOCKS,
                        1.0F + random.nextFloat(),
                        random.nextFloat() * 0.7F + 0.3F,
                        false
                );
            }
        }
        
        level.addParticle(ParticleTypes.SMALL_FLAME, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
    }
    
}
