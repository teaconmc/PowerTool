package org.teacon.powertool.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.teacon.powertool.entity.AutoVanishBoat;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutoVanishBoatItem extends BoatItem {
    
    public AutoVanishBoatItem(Boat.Type type, Properties properties) {
        super(false, type, properties);
    }
    
    public AutoVanishBoatItem(Boat.Type type) {
        this(type, new Properties());
    }
    
    @Override
    protected Boat getBoat(Level level, HitResult hitResult, ItemStack stack, Player player) {
        Vec3 vec3 = hitResult.getLocation();
        var boat = new AutoVanishBoat(level, vec3.x, vec3.y, vec3.z);
        if (level instanceof ServerLevel serverlevel) {
            EntityType.<Boat>createDefaultStackConfig(serverlevel, stack, player).accept(boat);
        }
        return boat;
    }
}
