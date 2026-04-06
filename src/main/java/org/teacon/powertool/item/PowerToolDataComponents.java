package org.teacon.powertool.item;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.entity.FenceKnotEntity;

import java.util.List;

public class PowerToolDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PowerTool.MODID);
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<CommandRune.DelayedCommandData>>> DELAYED_COMMANDS = DATA_COMPONENTS.register(
            "delayed_command", () -> DataComponentType.<List<CommandRune.DelayedCommandData>>builder()
                    .persistent(CommandRune.DelayedCommandData.CODEC.listOf()).build()
    );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CYCLE = DATA_COMPONENTS.register(
            "cycle", () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build()
    );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CONSUME = DATA_COMPONENTS.register(
            "consume", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> COMMAND = DATA_COMPONENTS.register(
            "command", () -> DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExamineHoloGlass.BlockComponents>> BLOCKS_DATA = DATA_COMPONENTS.register(
            "blocks_data", () -> DataComponentType.<ExamineHoloGlass.BlockComponents>builder()
                    .persistent(ExamineHoloGlass.BlockComponents.CODEC)
                    .networkSynchronized(ExamineHoloGlass.BlockComponents.STREAM_CODEC)
                    .build()
    );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ExamineHoloGlass.BlockTagsComponent>> BLOCK_TAGS_DATA = DATA_COMPONENTS.register(
            "block_tags", () -> DataComponentType.<ExamineHoloGlass.BlockTagsComponent>builder()
                    .persistent(ExamineHoloGlass.BlockTagsComponent.CODEC)
                    .networkSynchronized(ExamineHoloGlass.BlockTagsComponent.STREAM_CODEC)
                    .build()
    );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FenceKnotEntity.PowerToolKnotData>> KNOT_DATA = DATA_COMPONENTS.register(
            "knot_data", () -> DataComponentType.<FenceKnotEntity.PowerToolKnotData>builder()
                    .persistent(FenceKnotEntity.PowerToolKnotData.CODEC)
                    .networkSynchronized(FenceKnotEntity.PowerToolKnotData.STREAM_CODEC)
                    .build()
    );
}
