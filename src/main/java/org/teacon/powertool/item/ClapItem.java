package org.teacon.powertool.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.teacon.powertool.PowerToolSoundEvents;
import org.teacon.powertool.annotation.NonNullByDefault;

@NonNullByDefault
public class ClapItem extends Item {
    public ClapItem(Properties prop) {
        super(prop);
    }
    
    @Override
    public InteractionResult use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            var pitch = level.getRandom().nextInt(4, 14) * 0.1F;
            level.playSound(null, p.blockPosition(), PowerToolSoundEvents.CLAP.get(), SoundSource.PLAYERS, 40F, pitch);
            p.getCooldowns().addCooldown(BuiltInRegistries.ITEM.getKey(this), 5);
        }
        return InteractionResult.SUCCESS;
    }
}
