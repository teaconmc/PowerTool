package org.teacon.powertool.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.teacon.powertool.entity.FenceKnotEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TonkItem extends Item {
    
    public TonkItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide() && context.getPlayer() != null) {
            var player = context.getPlayer();
            var held = context.getItemInHand();
            var level = context.getLevel();
            var pos = context.getClickedPos();
            if(!level.getBlockState(pos).is(BlockTags.FENCES)){
                return InteractionResult.PASS;
            }
            var range = new AABB(pos.getX() - 7, pos.getY() - 7, pos.getZ() - 7, pos.getX() + 7, pos.getY() + 7, pos.getZ() + 7);
            for (var mob : level.getEntitiesOfClass(Mob.class, range)) {
                if (mob.getLeashHolder() == player) {
                    return InteractionResult.PASS;
                }
            }
            range = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            if (!level.getEntitiesOfClass(FenceKnotEntity.class, range).isEmpty()) {
                return InteractionResult.PASS;
            }
            var knot = new FenceKnotEntity(level, pos);
            level.addFreshEntity(knot);
            knot.playPlacementSound();
            level.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(player));
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
