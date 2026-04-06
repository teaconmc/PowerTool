package org.teacon.powertool.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.network.client.UpdateOpenMenuSourcePacket;

import java.util.OptionalInt;
import java.util.function.Consumer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    
    @Inject(
            method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onOpenMenu(MenuProvider menu, Consumer<RegistryFriendlyByteBuf> extraDataWriter, CallbackInfoReturnable<OptionalInt> cir) {
        ServerPlayer thiz = (ServerPlayer) (Object) this;
        if (thiz.getAbilities().instabuild) return;
        HitResult hitResult = thiz.pick(thiz.blockInteractionRange(), 0, false);
        if (hitResult != null) {
            if (hitResult instanceof BlockHitResult blockHitResult) {
                BlockPos pos = blockHitResult.getBlockPos();
                PacketDistributor.sendToPlayer(
                        thiz,
                        new UpdateOpenMenuSourcePacket(pos)
                );
            }
            return;
        }
        cir.cancel();
    }
}
