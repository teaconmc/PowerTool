package org.teacon.powertool.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.item.TransparentBrushItem;

@Mixin(ArmorStand.class)
public abstract class MixinArmorStand extends LivingEntity {
    
    protected MixinArmorStand(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    @Inject(method = "interactAt",at = @At("HEAD"),cancellable = true)
    public void onItemUse(Player player, Vec3 vec, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        var item = player.getItemInHand(hand);
        if(!(item.getItem() instanceof TransparentBrushItem)) return;
        if(player.isCrouching()){
            this.setInvisible(!this.isInvisible());
            cir.setReturnValue(InteractionResult.SUCCESS);
            cir.cancel();
        }
    }
}
