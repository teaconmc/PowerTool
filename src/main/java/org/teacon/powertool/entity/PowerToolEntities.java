package org.teacon.powertool.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.teacon.powertool.PowerTool;

import java.util.HashSet;
import java.util.Set;

public class PowerToolEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, PowerTool.MODID);
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZER = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, PowerTool.MODID);

    public static final DeferredHolder<EntityType<?>,EntityType<FenceKnotEntity>> FENCE_KNOT = ENTITIES.register("fence_knot", () ->
            EntityType.Builder.<FenceKnotEntity>of(FenceKnotEntity::new, MobCategory.MISC)
                    .sized(0.375F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("fence_knot"));

    public static final DeferredHolder<EntityDataSerializer<?>,EntityDataSerializer<Set<BlockPos>>> BLOCK_POS_LIST = ENTITY_DATA_SERIALIZER.register(
            "block_pos_list",() ->EntityDataSerializer.forValueType(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new))));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        ENTITY_DATA_SERIALIZER.register(bus);
    }
}
