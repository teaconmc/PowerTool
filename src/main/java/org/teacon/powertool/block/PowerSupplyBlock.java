package org.teacon.powertool.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.block.entity.PowerSupplyBlockEntity;
import org.teacon.powertool.menu.PowerSupplyMenu;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PowerSupplyBlock extends BaseEntityBlock {
    
    private static final MapCodec<PowerSupplyBlock> CODEC = simpleCodec(PowerSupplyBlock::new);
    
    public PowerSupplyBlock(Properties prop) {
        super(prop);
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return use(level, pos, player);
    }
    
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return VanillaUtils.itemInteractionFrom(use(level,pos,player));
    }
    
    
    public InteractionResult use(Level level, BlockPos pos, Player p) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (level.getBlockEntity(pos) instanceof PowerSupplyBlockEntity be) {
                p.openMenu( new PowerSupplyMenu.Provider(be.data),
                        buf -> buf.writeVarInt(be.data.status).writeVarInt(be.data.power));
            }
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PowerSupplyBlockEntity(pPos, pState);
    }

    public static final class Data {
        // Working status flag. 0 represents OFF; 1 represents ON.
        public int status = 1;
        // Amount of energy output per tick
        public int power = 100;
        public Runnable markDirty;
    }
}
