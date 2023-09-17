package org.teacon.powertool.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.teacon.powertool.PowerToolSoundEvents;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ClapItem extends Item {
    public ClapItem(Properties prop) {
        super(prop);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            var pitch = level.random.nextInt(4, 14) * 0.1F;
            level.playSound(null, p.blockPosition(), PowerToolSoundEvents.CLAP.get(), SoundSource.PLAYERS, 40F, pitch);
            p.getCooldowns().addCooldown(this, 5);
        }
        return InteractionResultHolder.sidedSuccess(p.getItemInHand(hand), level.isClientSide());
    }
}
