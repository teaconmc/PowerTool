package org.teacon.powertool.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.teacon.powertool.PowerToolConfig;

@Mixin(PlayerSpawnFinder.class)
public class PlayerSpawnFinderMixin {

    @WrapOperation(method = "findSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/PlayerSpawnFinder;fixupSpawnHeight(Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 doNotTheSpawnHeight(CollisionGetter level, BlockPos spawnPos, Operation<Vec3> original) {
        // 出于未知原因，TeaCon 2026 展示服中，Minecraft 会在试图「纠正」出生点海拔高度时一路降到 y = -63。
        // 我们暂时没时间找原因了，先写一个 Mixin 临时让他别「纠正」了。
        return PowerToolConfig.fixSpawnHeight.get() ? original.call(level, spawnPos) : Vec3.atBottomCenterOf(spawnPos);
    }
}
