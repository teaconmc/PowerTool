package org.teacon.powertool.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teacon.powertool.annotation.NonNullByDefault;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

@NonNullByDefault
@Mixin(PersistentEntitySectionManager.class)
public abstract class PersistentEntitySectionManagerMixin<T extends EntityAccess> {

    @Shadow
    public abstract LevelEntityGetter<T> getEntityGetter();

    @Inject(method = "gatherStats", at = @At("RETURN"), cancellable = true)
    private void powerTool$appendEntityTypeStats(CallbackInfoReturnable<String> cir) {
        Map<Identifier, Integer> entityTypeCounts = new TreeMap<>();
        for (T entityAccess : this.getEntityGetter().getAll()) {
            if (entityAccess instanceof Entity entity) {
                entityTypeCounts.merge(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()), 1, Integer::sum);
            }
        }
        StringJoiner stats = new StringJoiner(",", cir.getReturnValue() + ",entity_types={", "}");
        entityTypeCounts.forEach((entityType, count) -> stats.add(entityType + "=" + count));
        cir.setReturnValue(stats.toString());
    }
}
