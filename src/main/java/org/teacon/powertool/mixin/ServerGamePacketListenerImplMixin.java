package org.teacon.powertool.mixin;

import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Redirect(method = "handleSetCommandBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/Blocks;REPEATING_COMMAND_BLOCK:Lnet/minecraft/world/level/block/Block;"))
    private Block usePowerToolBlock(ServerboundSetCommandBlockPacket packet) {
        var blockEntity = this.player.level.getBlockEntity(packet.getPos());
        if (blockEntity instanceof PeriodicCommandBlockEntity) {
            return PowerToolBlocks.COMMAND_BLOCK.get();
        } else {
            return Blocks.REPEATING_COMMAND_BLOCK;
        }
    }
}
