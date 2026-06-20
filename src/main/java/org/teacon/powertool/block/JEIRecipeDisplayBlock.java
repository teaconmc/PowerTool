package org.teacon.powertool.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.entity.JEIRecipeDisplayBlockEntity;
import org.teacon.powertool.client.renders.JEIRecipeDisplayBlockEntityRenderer;

import java.util.function.Consumer;

@NonNullByDefault
public class JEIRecipeDisplayBlock extends BaseEntityBlock implements WithTooltip {

    private static final MapCodec<JEIRecipeDisplayBlock> CODEC = simpleCodec(JEIRecipeDisplayBlock::new);

    protected JEIRecipeDisplayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level instanceof Level l && l.isClientSide) {
            return ClientShapeHelper.getShape(level, pos);
        }
        return super.getShape(state, level, pos, context);
    }
    
    @Override
    public boolean hasDynamicShape() {
        return true;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JEIRecipeDisplayBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return use(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return use(level, pos, player);
    }

    private InteractionResult use(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (player.getAbilities().instabuild && level.getBlockEntity(pos) instanceof JEIRecipeDisplayBlockEntity be) {
                player.openMenu(be,buf -> buf.writeBlockPos(pos));
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("block.powertool.jei_recipe_display_block.tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static class ClientShapeHelper {
        private static VoxelShape getShape(BlockGetter level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof JEIRecipeDisplayBlockEntity be) {
                if (be.recipeType != null && be.recipeId != null) {
                    var cache = JEIRecipeDisplayBlockEntityRenderer.recipeLayoutCache;
                    if (cache != null) {
                        var key = new JEIRecipeDisplayBlockEntityRenderer.RecipeKey(be.recipeType, be.recipeId, pos);
                        var entry = cache.getMap().get(key);
                        if (entry != null && entry.valid()) {
                            return Shapes.create(entry.getWorldCorners(BlockPos.ZERO, be.yRotation).getAABB().inflate(0.04f));
                        }
                    }
                }
            }
            return Shapes.block();
        }
    }
}
