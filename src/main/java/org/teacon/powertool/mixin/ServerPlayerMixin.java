package org.teacon.powertool.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.api.IServerPlayerInteractingBlockPos;
import org.teacon.powertool.network.client.UpdateOpenMenuSourcePacket;

import java.util.OptionalInt;
import java.util.function.Consumer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IServerPlayerInteractingBlockPos {

    @Unique
    private @Nullable BlockPos powerTool$interactingBlockPos;

    @Override
    public void powerTool$startInteractingBlockPos(BlockPos pos) {
        powerTool$interactingBlockPos = pos;
    }

    @Override
    public void powerTool$endInteractingBlockPos() {
        powerTool$interactingBlockPos = null;
    }

    @Override
    public BlockPos powerTool$getInteractingBlockPos() {
        return powerTool$interactingBlockPos;
    }

    @Inject(
            method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At("HEAD")
    )
    private void onOpenMenu(MenuProvider menu, Consumer<RegistryFriendlyByteBuf> extraDataWriter, CallbackInfoReturnable<OptionalInt> cir) {
        ServerPlayer thiz = (ServerPlayer) (Object) this;
        if (thiz.getAbilities().instabuild) return;
        BlockPos pos = powerTool$getInteractingBlockPos();
        if (pos != null) {
            PacketDistributor.sendToPlayer(
                    thiz,
                    new UpdateOpenMenuSourcePacket(pos)
            );
        }
    }
}
