package org.teacon.powertool.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.MinecartDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.entity.AutoVanishMinecart;
import org.teacon.powertool.entity.PowerToolEntities;

import javax.annotation.Nullable;

@NonNullByDefault
public class AutoVanishMinecartItem extends Item {
    
    public AutoVanishMinecartItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }
    
    private static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new MinecartDispenseItemBehavior(PowerToolEntities.AUTO_VANISH_MINECART.get());
    
    private static AbstractMinecart createMinecart(
            ServerLevel level,
            double x,
            double y,
            double z,
            
            ItemStack stack,
            @Nullable Player player
    ) {
        var abstractMinecart = new AutoVanishMinecart(level, x, y, z);
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
                        ? ((BaseRailBlock) blockstate.getBlock()).getRailDirection(blockstate, level, blockpos, null)
                        : RailShape.NORTH_SOUTH;
                double d0 = 0.0;
                if (railshape.isSlope()) {
                    d0 = 0.5;
                }
                
                AbstractMinecart abstractminecart = createMinecart(
                        serverlevel,
                        (double) blockpos.getX() + 0.5,
                        (double) blockpos.getY() + 0.0625 + d0,
                        (double) blockpos.getZ() + 0.5,
                        itemstack,
                        context.getPlayer()
                );
                serverlevel.addFreshEntity(abstractminecart);
                serverlevel.gameEvent(GameEvent.ENTITY_PLACE, blockpos, GameEvent.Context.of(context.getPlayer(), serverlevel.getBlockState(blockpos.below())));
            }
            
            itemstack.shrink(1);
            return InteractionResult.SUCCESS;
        }
    }
}
