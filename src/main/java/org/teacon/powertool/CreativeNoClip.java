package org.teacon.powertool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.client.CreativeNoClipClient;
import org.teacon.powertool.network.client.UpdateCreativeNoClipPacket;

public final class CreativeNoClip {
    
    private CreativeNoClip() {
    }
    
    public static boolean canNoClip(Player player) {
        return player.isSpectator() || enabled(player) && player.isCreative() && player.getAbilities().flying;
    }
    
    public static boolean enabled(Player player) {
        if (player.level().isClientSide && FMLEnvironment.getDist() == Dist.CLIENT) {
            return CreativeNoClipClient.enabled(player);
        }
        return player.getData(PowerToolAttachments.CREATIVE_NO_CLIP);
    }
    
    public static void setEnabled(ServerPlayer player, boolean enabled) {
        player.setData(PowerToolAttachments.CREATIVE_NO_CLIP, enabled);
        sync(player);
    }
    
    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new UpdateCreativeNoClipPacket(enabled(player)));
    }
}
