package org.teacon.powertool.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.teacon.powertool.PowerTool;

import java.util.LinkedHashSet;
import java.util.Set;

public class PowerToolEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PowerTool.MODID);

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, PowerTool.MODID);

    public static final RegistryObject<EntityType<FenceKnotEntity>> FENCE_KNOT = ENTITIES.register("fence_knot", () ->
            EntityType.Builder.<FenceKnotEntity>of(FenceKnotEntity::new, MobCategory.MISC)
                    .sized(0.375F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("fence_knot"));

    public static final RegistryObject<EntityDataSerializer<Set<BlockPos>>> BLOCK_POS_LIST = ENTITY_DATA_SERIALIZERS.register("block_pos_list", () ->
            new EntityDataSerializer.ForValueType<>() {
                @Override
                public void write(FriendlyByteBuf buf, Set<BlockPos> poses) {
                    buf.writeVarInt(poses.size());
                    for (var pos : poses) {
                        buf.writeBlockPos(pos);
                    }
                }

                @Override
                public Set<BlockPos> read(FriendlyByteBuf buf) {
                    final int count = buf.readVarInt();
                    var ret = new LinkedHashSet<BlockPos>(count);
                    for (int i = 0; i < count; i++) {
                        ret.add(buf.readBlockPos());
                    }
                    return ret;
                }
            });

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        ENTITY_DATA_SERIALIZERS.register(bus);
    }
}
