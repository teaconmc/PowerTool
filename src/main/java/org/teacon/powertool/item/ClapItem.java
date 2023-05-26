package org.teacon.powertool.item;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.teacon.powertool.PowerToolSoundEvents;

public class ClapItem extends Item {
    public ClapItem(Properties prop) {
        super(prop);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        if (p.level instanceof ServerLevel sl) {
            var playersToNotify = sl.getChunkSource().chunkMap.getPlayers(p.chunkPosition(), false);
            for (var otherPlayer : playersToNotify) {
                otherPlayer.connection.send(new ClientboundSoundPacket(PowerToolSoundEvents.CLAP.getHolder().get(), SoundSource.PLAYERS,
                        p.getX(), p.getY(), p.getZ(), 1F, 0.5F, 0L));
            }
        }
        return InteractionResultHolder.sidedSuccess(p.getItemInHand(hand), level.isClientSide());
    }
}
