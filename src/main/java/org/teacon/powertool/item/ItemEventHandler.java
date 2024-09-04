package org.teacon.powertool.item;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.entity.FenceKnotEntity;

@EventBusSubscriber(modid = PowerTool.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ItemEventHandler {

    @SubscribeEvent
    public static void on(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == LogicalSide.SERVER) {
            var player = event.getEntity();
            var held = player.getItemInHand(event.getHand());
            if (held.getItem() != Items.LEAD) {
                return;
            }
            var level = event.getLevel();
            var pos = event.getPos();
            var range = new AABB(pos.getX() - 7, pos.getY() - 7, pos.getZ() - 7, pos.getX() + 7, pos.getY() + 7, pos.getZ() + 7);
            for (var mob : level.getEntitiesOfClass(Mob.class, range)) {
                if (mob.getLeashHolder() == player) {
                    return;
                }
            }
            range = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            if (!level.getEntitiesOfClass(FenceKnotEntity.class, range).isEmpty()) {
                return;
            }
            event.setCanceled(true);
            if(event.getLevel().getBlockState(pos).is(BlockTags.FENCES)) {
                var knot = new FenceKnotEntity(level, pos);
                level.addFreshEntity(knot);
                knot.playPlacementSound();
                level.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(player));
                if (!player.getAbilities().instabuild) {
                held.shrink(1);
                }
            }
        }
    }
}
