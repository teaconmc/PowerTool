package org.teacon.powertool.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.teacon.powertool.entity.AutoVanishMinecart;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutoVanishMinecartItem extends Item {
    
    public AutoVanishMinecartItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }
    
    private static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
        
        @Override
        public ItemStack execute(BlockSource p_302448_, ItemStack itemStack) {
            Direction direction = p_302448_.state().getValue(DispenserBlock.FACING);
            ServerLevel serverlevel = p_302448_.level();
            Vec3 vec3 = p_302448_.center();
            double d0 = vec3.x() + (double)direction.getStepX() * 1.125;
            double d1 = Math.floor(vec3.y()) + (double)direction.getStepY();
            double d2 = vec3.z() + (double)direction.getStepZ() * 1.125;
            BlockPos blockpos = p_302448_.pos().relative(direction);
            BlockState blockstate = serverlevel.getBlockState(blockpos);
            RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock
                    ? ((BaseRailBlock)blockstate.getBlock()).getRailDirection(blockstate, serverlevel, blockpos, null)
                    : RailShape.NORTH_SOUTH;
            double d3;
            if (blockstate.is(BlockTags.RAILS)) {
                if (railshape.isAscending()) {
                    d3 = 0.6;
                } else {
                    d3 = 0.1;
                }
            } else {
                if (!blockstate.isAir() || !serverlevel.getBlockState(blockpos.below()).is(BlockTags.RAILS)) {
                    return this.defaultDispenseItemBehavior.dispense(p_302448_, itemStack);
                }
                
                BlockState blockstate1 = serverlevel.getBlockState(blockpos.below());
                RailShape railshape1 = blockstate1.getBlock() instanceof BaseRailBlock
                        ? ((BaseRailBlock)blockstate1.getBlock()).getRailDirection(blockstate1, serverlevel, blockpos.below(), null)
                        : RailShape.NORTH_SOUTH;
                if (direction != Direction.DOWN && railshape1.isAscending()) {
                    d3 = -0.4;
                } else {
                    d3 = -0.9;
                }
            }
            
            AbstractMinecart abstractminecart = createMinecart(
                    serverlevel, d0, d1 + d3, d2,  itemStack, null
            );
            serverlevel.addFreshEntity(abstractminecart);
            itemStack.shrink(1);
            return itemStack;
        }
        
        @Override
        protected void playSound(BlockSource p_302470_) {
            p_302470_.level().levelEvent(1000, p_302470_.pos(), 0);
        }
    };
    
    private static AbstractMinecart createMinecart(
            ServerLevel level,
            double x,
            double y,
            double z,

            ItemStack stack,
            @Nullable Player player
    ) {
        var abstractMinecart = new AutoVanishMinecart(level,x,y,z);
        EntityType.<AbstractMinecart>createDefaultStackConfig(level, stack, player).accept(abstractMinecart);
        return abstractMinecart;
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (!blockstate.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        } else {
            ItemStack itemstack = context.getItemInHand();
            if (level instanceof ServerLevel serverlevel) {
                RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock
                        ? ((BaseRailBlock)blockstate.getBlock()).getRailDirection(blockstate, level, blockpos, null)
                        : RailShape.NORTH_SOUTH;
                double d0 = 0.0;
                if (railshape.isAscending()) {
                    d0 = 0.5;
                }
                
                AbstractMinecart abstractminecart = createMinecart(
                        serverlevel,
                        (double)blockpos.getX() + 0.5,
                        (double)blockpos.getY() + 0.0625 + d0,
                        (double)blockpos.getZ() + 0.5,
                        itemstack,
                        context.getPlayer()
                );
                serverlevel.addFreshEntity(abstractminecart);
                serverlevel.gameEvent(GameEvent.ENTITY_PLACE, blockpos, GameEvent.Context.of(context.getPlayer(), serverlevel.getBlockState(blockpos.below())));
            }
            
            itemstack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }
}
