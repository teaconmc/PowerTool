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
    
    public static final DeferredHolder<EntityType<?>,EntityType<AutoVanishBoat>> AUTO_VANISH_BOAT = ENTITIES.register("auto_vanish_boat",
            () -> EntityType.Builder.<AutoVanishBoat>of(AutoVanishBoat::new,MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
                    .build("auto_vanish_boat"));
    
    public static final DeferredHolder<EntityType<?>,EntityType<AutoVanishMinecart>> AUTO_VANISH_MINECART = ENTITIES.register("auto_vanish_minecart",
            () -> EntityType.Builder.<AutoVanishMinecart>of(AutoVanishMinecart::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .passengerAttachments(0.1875F)
                    .clientTrackingRange(8)
                    .build("auto_vanish_minecart"));

    public static final DeferredHolder<EntityDataSerializer<?>,EntityDataSerializer<Set<BlockPos>>> BLOCK_POS_LIST = ENTITY_DATA_SERIALIZER.register(
            "block_pos_list",() ->EntityDataSerializer.forValueType(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new))));

    public static final DeferredHolder<EntityDataSerializer<?>,EntityDataSerializer<FenceKnotEntity.Type>> FENCE_KNOT_TYPE = ENTITY_DATA_SERIALIZER.register(
            "fence_knot_type",() -> EntityDataSerializer.forValueType(FenceKnotEntity.Type.STREAM_CODEC)
    );
    
    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        ENTITY_DATA_SERIALIZER.register(bus);
    }
}
