package org.teacon.powertool.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.entity.FenceKnotEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = PowerTool.MODID)
public class PowerToolItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, PowerTool.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PowerTool.MODID);
    
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,PowerTool.MODID);
    
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIAL = DeferredRegister.create(Registries.ARMOR_MATERIAL,PowerTool.MODID);

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> THE_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.powertool"))
            .icon(() -> new ItemStack(PowerToolBlocks.COMMAND_BLOCK.get()))
            .withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS, CreativeModeTabs.INGREDIENTS, CreativeModeTabs.SPAWN_EGGS)
            .build());
    
    public static final DeferredHolder<ArmorMaterial,ArmorMaterial> HOLO_GLASS_ARMOR_MATERIAL = ARMOR_MATERIAL.register("holo_glass",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class),map -> {
                for(var type : ArmorItem.Type.values()) {
                    map.put(type,0);
                }
            }),
                    0,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.EMPTY,
                    List.of(),
                    0f,0f));

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<FenceKnotEntity.PowerToolKnotData>> KNOT_DATA = DATA_COMPONENTS.register(
            "knot_data",() -> DataComponentType.<FenceKnotEntity.PowerToolKnotData>builder()
                    .persistent(FenceKnotEntity.PowerToolKnotData.CODEC)
                    .networkSynchronized(FenceKnotEntity.PowerToolKnotData.STREAM_CODEC)
                    .build()
    );
    
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<ExamineHoloGlass.BlockTagsComponent>> BLOCK_TAGS_DATA = DATA_COMPONENTS.register(
            "block_tags",() -> DataComponentType.<ExamineHoloGlass.BlockTagsComponent>builder()
                    .persistent(ExamineHoloGlass.BlockTagsComponent.CODEC)
                    .networkSynchronized(ExamineHoloGlass.BlockTagsComponent.STREAM_CODEC)
                    .build()
    );
    
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<ExamineHoloGlass.BlockComponents>> BLOCKS_DATA = DATA_COMPONENTS.register(
            "blocks_data",() -> DataComponentType.<ExamineHoloGlass.BlockComponents>builder()
                    .persistent(ExamineHoloGlass.BlockComponents.CODEC)
                    .networkSynchronized(ExamineHoloGlass.BlockComponents.STREAM_CODEC)
                    .build()
    );
    
    
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        ITEMS.register("useless_stick", () -> new Item(new Item.Properties()) {
            @Override
            public boolean isFoil(ItemStack stack) {
                return true;
            }
        });
        ITEMS.register("clap", () -> new ClapItem(new Item.Properties()));
        ITEMS.register("clap_but_sad", () -> new ClapItem(new Item.Properties()));
        ITEMS.register("clap_but_angry", () -> new ClapItem(new Item.Properties()));
        ITEMS.register("transparent_brush",TransparentBrushItem::new);
        ITEMS.register("examine_holo_glass",ExamineHoloGlass::new);
        CREATIVE_MODE_TABS.register(bus);
        DATA_COMPONENTS.register(bus);
        ARMOR_MATERIAL.register(bus);
    }

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == THE_TAB.get()) {
            for (var regObj : ITEMS.getEntries()) {
                event.accept(regObj.get());
            }
        }
    }
}
