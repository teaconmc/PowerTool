package org.teacon.powertool.block.holo_sign;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.network.client.OpenHolographicSignEditor;
import org.teacon.powertool.network.server.UpdateBlockEntityData;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HolographicSignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

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
        builder.add(WATERLOGGED,LIT);
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
        if(hand == InteractionHand.MAIN_HAND){
            return VanillaUtils.itemInteractionFrom(use(level,pos,player));
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
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        
    }
    
    @Override
    public boolean hasDynamicShape() {
        return true;
    }
    
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(type == SignType.COMMON && context instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof Player player && !player.getAbilities().instabuild){
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
        }
        else if(!player.getAbilities().instabuild || player.isCrouching()){
            if(level.isClientSide()){
                return ClientLogicHolder.tryUseAdditional(level,pos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            else if(level.getBlockEntity(pos) instanceof RawJsonHolographicSignBlockEntity be){
                for(var component : be.forRender){
                    var clickEvent = component.getStyle().getClickEvent();
                    if(clickEvent == null) return InteractionResult.PASS;
                    var action = clickEvent.getAction();
                    if(action == ClickEvent.Action.RUN_COMMAND){
                        VanillaUtils.runCommand(clickEvent.getValue(),player);
                    }
                }
                
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if(this.type == SignType.RAW_JSON && level.isClientSide() && level.getBlockEntity(currentPos) instanceof RawJsonHolographicSignBlockEntity sign){
            PacketDistributor.sendToServer(UpdateBlockEntityData.create(sign));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if(type == SignType.RAW_JSON){
            tooltipComponents.add(Component.translatable(""));
        }
    }
    
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private static final class ClientLogicHolder {
        public static boolean tryOpenURL(String url){
            try {
                URI uri;
                try {
                    uri = Util.parseAndValidateUntrustedUri(url);
                }catch (URISyntaxException e) {
                    uri = Util.parseAndValidateUntrustedUri("https://" + url);
                }
                var mc = Minecraft.getInstance();
                if (mc.options.chatLinksPrompt().get()) {
                    URI finalUri = uri;
                    mc.setScreen(new ConfirmLinkScreen(p_351659_ -> {
                        if (p_351659_) {
                            Util.getPlatform().openUri(finalUri);
                        }
                        mc.setScreen(null);
                    }, url, false));
                } else {
                    Util.getPlatform().openUri(uri);
                }
                return true;
            } catch (URISyntaxException e) {
                return false;
            }
        }

        public static boolean tryUseAdditional(Level level, BlockPos pos) {
            if(level.isClientSide() && level.getBlockEntity(pos) instanceof LinkHolographicSignBlockEntity be) {
                return tryOpenURL(be.url);
            }
            if(level.isClientSide() && level.getBlockEntity(pos) instanceof RawJsonHolographicSignBlockEntity be) {
                for(var component : be.forRender){
                    var clickEvent = component.getStyle().getClickEvent();
                    if(clickEvent == null) return false;
                    var action = clickEvent.getAction();
                    if(action == ClickEvent.Action.OPEN_URL) return tryOpenURL(clickEvent.getValue());
                    if(action == ClickEvent.Action.OPEN_FILE){
                        Util.getPlatform().openFile(new File(clickEvent.getValue()));
                        return true;
                    }
                    if(action == ClickEvent.Action.COPY_TO_CLIPBOARD){
                        Minecraft.getInstance().keyboardHandler.setClipboard(clickEvent.getValue());
                        return true;
                    }
                    //交给服务端
                    //if(action == ClickEvent.Action.RUN_COMMAND)
                    if(action == ClickEvent.Action.SUGGEST_COMMAND){
                        var screen = new ChatScreen("");
                        screen.handleComponentClicked(component.getStyle());
                        Minecraft.getInstance().setScreen(screen);
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
