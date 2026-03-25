package org.teacon.powertool.block;

import com.mojang.serialization.MapCodec;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.entity.RegisterBlockEntity;
import org.teacon.powertool.menu.RegisterMenu;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * 收银台（Register）可接受指定物品并输出红石信号脉冲。
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegisterBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 13, 16);
    public static final MapCodec<RegisterBlock> CODEC = simpleCodec(RegisterBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final Direction[] ALL_DIRECTIONS = Direction.values();

    public RegisterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.FALSE));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<RegisterBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RegisterBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, Boolean.FALSE), UPDATE_ALL);
        } else {
            level.setBlock(pos, state.setValue(POWERED, Boolean.TRUE), UPDATE_ALL);
            level.scheduleTick(pos, this, 2);
        }
        for(var dir : VanillaUtils.DIRECTIONS) {
            level.updateNeighborsAt(pos.relative(dir),this);
        }
        // Trigger block update oo all 6 faces
        for (Direction direction : ALL_DIRECTIONS) {
            level.neighborChanged(pos.relative(direction), this, pos);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof RegisterBlockEntity theBE) {
            if (player.getAbilities().instabuild) {
                player.openMenu(new RegisterMenu.Provider(theBE.menuView,pos),buf -> buf.writeBlockPos(pos));
                return InteractionResult.SUCCESS;
            }
            if (theBE.itemToAccept.isEmpty()) {
                return InteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            boolean accept;
            if (theBE.matchDataComponents) {
                accept = ItemStack.isSameItemSameComponents(stack, theBE.itemToAccept);
            } else {
                accept = stack.is(theBE.itemToAccept.getItem());
            }
            accept &= stack.getCount() >= theBE.itemToAccept.getCount();
            if (accept) {
                stack.shrink(theBE.itemToAccept.getCount());
                if(!theBE.itemToSupply.isEmpty()) player.getInventory().add(theBE.itemToSupply.copy());
                level.scheduleTick(pos, state.getBlock(), 2);
                return InteractionResult.SUCCESS;
            } else if (!player.getAbilities().instabuild){
                player.displayClientMessage(Component.translatable("block.powertool.register.hud.insufficient"), true);
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // TODO[3TUSK]: 打开 GUI
        // 是不是可以去掉这个todo了[xkball]
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getSignal(blockAccess, pos, side);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
