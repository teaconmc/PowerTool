package org.teacon.powertool.block.holo_sign;

import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.network.client.OpenHolographicSignEditor;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.URI;
import java.net.URISyntaxException;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HolographicSignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    
    public final SignType type;

    public HolographicSignBlock(Properties prop, SignType type) {
        super(prop);
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE));
    }
    
    @Override
    //todo 返回一个可用的MapCodec
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return type.newBlockEntity(pos, state);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return use(level, pos, player);
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return VanillaUtils.itemInteractionFrom(use(level,pos,player));
    }
    
    public InteractionResult use(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp && sp.getAbilities().instabuild && !player.isCrouching()) {
            PacketDistributor.sendToPlayer(sp,new OpenHolographicSignEditor(pos,type));
        }
        else if(!player.getAbilities().instabuild || player.isCrouching()){
            return tryOpenLink(level,pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }
    
    public boolean tryOpenLink(Level level, BlockPos pos) {
        if(level.isClientSide() && level.getBlockEntity(pos) instanceof LinkHolographicSignBlockEntity be) {
            try {
                URI uri;
                try {
                    uri = Util.parseAndValidateUntrustedUri(be.url);
                }catch (URISyntaxException e) {
                    uri = Util.parseAndValidateUntrustedUri("https://" + be.url);
                }
                var mc = Minecraft.getInstance();
                if (mc.options.chatLinksPrompt().get()) {
                    URI finalUri = uri;
                    mc.setScreen(new ConfirmLinkScreen(p_351659_ -> {
                        if (p_351659_) {
                            Util.getPlatform().openUri(finalUri);
                        }
                        mc.setScreen(null);
                    }, be.url, false));
                } else {
                    Util.getPlatform().openUri(uri);
                }
                return true;
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
