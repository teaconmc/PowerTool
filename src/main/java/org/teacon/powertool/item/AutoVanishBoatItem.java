package org.teacon.powertool.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.entity.AutoVanishBoat;

@NonNullByDefault
public class AutoVanishBoatItem extends BoatItem {
    
    public AutoVanishBoatItem(Properties properties, EntityType<? extends AbstractBoat> type) {
        super(type, properties);
    }
    
    @Override
    public AutoVanishBoat getBoat(Level level, HitResult hitResult, ItemStack stack, Player player) {
        Vec3 vec3 = hitResult.getLocation();
        var boat = new AutoVanishBoat(level, vec3.x, vec3.y, vec3.z);
        boat.setWrapped(this.entityType);
        if (level instanceof ServerLevel serverlevel) {
            EntityType.<AutoVanishBoat>createDefaultStackConfig(serverlevel, stack, player).accept(boat);
        }
        return boat;
    }
}
