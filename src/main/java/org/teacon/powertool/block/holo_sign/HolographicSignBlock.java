package org.teacon.powertool.block.holo_sign;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.WithTooltip;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.network.client.OpenHolographicSignEditor;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.utils.VanillaUtils;

import java.net.URI;
import java.util.function.Consumer;

@NonNullByDefault
public class HolographicSignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, WithTooltip {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    
    public static final MapCodec<HolographicSignBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(propertiesCodec(), SignType.CODEC.fieldOf("type").forGetter(block -> block.type))
                    .apply(instance, HolographicSignBlock::new)
    );
    
    public final SignType type;
    
    public HolographicSignBlock(Properties prop, SignType type) {
        super(prop);
        this.type = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(LIT, Boolean.TRUE));
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, LIT);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return type.newBlockEntity(pos, state);
    }
    
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.MAIN_HAND) {
            return use(level, pos, player);
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }
    
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }
    
    @Override
    public boolean hasDynamicShape() {
        return true;
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (type == SignType.COMMON && context instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof Player player && !player.getAbilities().instabuild) {
            return Shapes.empty();
        }
        return super.getShape(state, level, pos, context);
    }
    
    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }
    
    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }
    
    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1f;
    }
    
    public InteractionResult use(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp
                && sp.getAbilities().instabuild && !player.isCrouching()) {
            PacketDistributor.sendToPlayer(sp, new OpenHolographicSignEditor(pos, type));
        } else if (!player.getAbilities().instabuild || player.isCrouching()) {
            if (level.isClientSide()) {
                return ClientLogicHolder.tryUseAdditional(level, pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
            } else if (level.getBlockEntity(pos) instanceof RawJsonHolographicSignBlockEntity be) {
                for (var component : be.forRender) {
                    var clickEvent = component.getStyle().getClickEvent();
                    if (clickEvent == null) return InteractionResult.PASS;
                    if (clickEvent instanceof ClickEvent.RunCommand(String command)) {
                        VanillaUtils.runCommand(command, player);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (this.type == SignType.RAW_JSON && level.isClientSide() && level.getBlockEntity(pos) instanceof RawJsonHolographicSignBlockEntity sign) {
            ClientPacketDistributor.sendToServer(UpdateBlockEntityData.create(sign));
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }
    
    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        if (type == SignType.RAW_JSON) {
            builder.accept(Component.translatable(""));
        }
    }
    
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    
    private static final class ClientLogicHolder {
        public static boolean tryOpenURL(URI uri) {
            var mc = Minecraft.getInstance();
            if (mc.options.chatLinksPrompt().get()) {
                mc.setScreen(new ConfirmLinkScreen(p_351659_ -> {
                    if (p_351659_) {
                        Util.getPlatform().openUri(uri);
                    }
                    mc.setScreen(null);
                }, uri.toString(), false));
            } else {
                Util.getPlatform().openUri(uri);
            }
            return true;
        }
        
        public static boolean tryUseAdditional(Level level, BlockPos pos) {
            if (level.isClientSide() && level.getBlockEntity(pos) instanceof LinkHolographicSignBlockEntity be) {
                try {
                    return tryOpenURL(URI.create(be.url));
                } catch (Exception e) {
                    LOGGER.warn("Failed to open link holographic sign block", e);
                }
            }
            if (level.isClientSide() && level.getBlockEntity(pos) instanceof RawJsonHolographicSignBlockEntity be) {
                for (var component : be.forRender) {
                    var clickEvent = component.getStyle().getClickEvent();
                    if (clickEvent == null) return false;
                    if (clickEvent instanceof ClickEvent.OpenUrl(URI uri)) return tryOpenURL(uri);
                    if (clickEvent instanceof ClickEvent.OpenFile of) {
                        Util.getPlatform().openFile(of.file());
                        return true;
                    }
                    if (clickEvent instanceof ClickEvent.CopyToClipboard(String value)) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(value);
                        return true;
                    }
                    //交给服务端
                    //if(action == ClickEvent.Action.RUN_COMMAND)
                    if (clickEvent instanceof ClickEvent.SuggestCommand(String command)) {
                        var screen = new ChatScreen("", true);
                        screen.insertText(command, false);
                        Minecraft.getInstance().setScreen(screen);
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
