package org.teacon.powertool.item;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.entity.FenceKnotEntity;

@Mod.EventBusSubscriber(modid = PowerTool.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
            event.setCanceled(true);
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
