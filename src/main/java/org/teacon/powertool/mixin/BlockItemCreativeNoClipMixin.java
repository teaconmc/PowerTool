package org.teacon.powertool.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.teacon.powertool.CreativeNoClip;

@Mixin(BlockItem.class)
public abstract class BlockItemCreativeNoClipMixin {
    
    @Redirect(
            method = "canPlace",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;isUnobstructed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Z"
            )
    )
    private boolean ignoreCreativeNoClipPlayer(Level level, BlockState state, BlockPos pos, CollisionContext context, BlockPlaceContext outerContext, BlockState outerState) {
        Player player = outerContext.getPlayer();
        if (player != null && CreativeNoClip.canNoClip(player)) {
            VoxelShape voxelShape = state.getCollisionShape(level, pos, context);
            return voxelShape.isEmpty() || level.isUnobstructed(player, voxelShape.move(pos.getX(), pos.getY(), pos.getZ()));
        }
        return level.isUnobstructed(state, pos, context);
    }
}
