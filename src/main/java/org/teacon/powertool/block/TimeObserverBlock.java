package org.teacon.powertool.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.entity.TimeObserverBlockEntity;
import org.teacon.powertool.network.client.OpenBlockScreen;
import org.teacon.powertool.utils.time.DailyCycleTimeSection;
import org.teacon.powertool.utils.time.ITimeSection;
import org.teacon.powertool.utils.time.InWorldDailyCycleTimeSection;
import org.teacon.powertool.utils.time.TimestampTimeSection;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimeObserverBlock extends BaseEntityBlock {
    
    public static final MapCodec<TimeObserverBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(propertiesCodec(), Type.CODEC.fieldOf("type").forGetter(block -> block.type))
                    .apply(instance, TimeObserverBlock::new)
    );
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty INVALIDATED = BooleanProperty.create("invalidated");
    
    protected final Type type;
    
    protected TimeObserverBlock(Properties properties, Type type) {
        super(properties);
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(INVALIDATED, false));
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide() && player.getAbilities().instabuild && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,new OpenBlockScreen(pos,type.getGuiID()));
        }
        return ItemInteractionResult.SUCCESS;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, INVALIDATED);
    }
    
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var facing = context.getNearestLookingDirection();
        var invalid = context.getLevel().getSignal(context.getClickedPos().relative(facing.getOpposite()),facing.getOpposite()) > 0;
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(INVALIDATED,invalid);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        var result = new TimeObserverBlockEntity(pos, state);
        result.setType(this.type);
        return result;
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
        return blockState.getValue(POWERED) && blockState.getValue(FACING) == side ? 15 : 0;
    }
    
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        level.scheduleTick(pos, this, 2);
    }
    
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        var facing = state.getValue(FACING);
        var invalid = level.getSignal(pos.relative(facing),facing) > 0;
        level.setBlock(pos, state.setValue(INVALIDATED,invalid).setValue(POWERED, !invalid && state.getValue(POWERED)), Block.UPDATE_ALL);
        var te = level.getBlockEntity(pos);
        if(te instanceof TimeObserverBlockEntity _te) _te.resetDelay();
        updateNeighborsInFront(level,pos,state);
    }
    
    public void updateNeighborsInFront(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        level.neighborChanged(blockpos, this, pos);
        level.updateNeighborsAtExceptFromFacing(blockpos, this, direction);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType,PowerToolBlocks.TIME_OBSERVER_BLOCK_ENTITY.get(),TimeObserverBlockEntity::tick);
    }
    
    public enum Type implements StringRepresentable {
        REAL_TIME(OpenBlockScreen.REAL_TIME_OBSERVER){
            private static final TimestampTimeSection BRIDGE = new TimestampTimeSection(0,0);
            
            @Override
            public ITimeSection readFromTE(TimeObserverBlockEntity te, CompoundTag tag, HolderLookup.Provider registries) {
                return BRIDGE.load(tag, registries);
            }
            
            @Override
            public boolean checkType(ITimeSection timeSection) {
                return timeSection instanceof TimestampTimeSection;
            }
        },
        REAL_DAILY_CYCLE(OpenBlockScreen.REAL_TIME_CYCLE_OBSERVER){
            private static final DailyCycleTimeSection BRIDGE = new DailyCycleTimeSection(0,0,0);
            
            @Override
            public ITimeSection readFromTE(TimeObserverBlockEntity te, CompoundTag tag, HolderLookup.Provider registries) {
                return BRIDGE.load(tag, registries);
            }
            
            @Override
            public boolean checkType(ITimeSection timeSection) {
                return timeSection instanceof DailyCycleTimeSection;
            }
        },
        GAME_DAILY_CYCLE(OpenBlockScreen.GAME_TIME_CYCLE_OBSERVER){
            @Override
            public ITimeSection readFromTE(TimeObserverBlockEntity te, CompoundTag tag, HolderLookup.Provider registries) {
                 return new InWorldDailyCycleTimeSection(te::getLevel,0,0).load(tag,registries);
            }
            
            @Override
            public boolean checkType(ITimeSection timeSection) {
                return timeSection instanceof InWorldDailyCycleTimeSection;
            }
        };
        
        private final int gui_id;
        
        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        
        Type(int guiId) {
            gui_id = guiId;
        }
        
        public int getGuiID() {
            return gui_id;
        }
        
        public abstract boolean checkType(ITimeSection timeSection);
        
        public abstract ITimeSection readFromTE(TimeObserverBlockEntity te, CompoundTag tag, HolderLookup.Provider registries);
        
        public CompoundTag write(@Nullable ITimeSection timeSection, HolderLookup.Provider registries){
            var tag = new CompoundTag();
            if(timeSection != null)timeSection.save(tag, registries);
            return tag;
        }
        
        @Override
        @NotNull
        public String getSerializedName() {
            return name();
        }
    }
}
