package org.teacon.powertool.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.ICosmeticBlock;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.entity.FenceKnotEntity;
import org.teacon.powertool.entity.MartingCarEntity;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber(modid = PowerTool.MODID)
public class PowerToolItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, PowerTool.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PowerTool.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.powertool"))
        .icon(() -> new ItemStack(PowerToolBlocks.COMMAND_BLOCK.get()))
        .withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS, CreativeModeTabs.INGREDIENTS, CreativeModeTabs.SPAWN_EGGS)
        .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COSMETIC_TAB = CREATIVE_MODE_TABS.register("cosmetic_tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.powertool.cosmetic"))
        .icon(() -> Blocks.BEACON.asItem().getDefaultInstance())
        .withTabsBefore(VanillaUtils.modRL("tab"))
        .build());
    

    public static DeferredHolder<Item, TonkItem> TONK, THICK_TONK, EXTRA_THICK_TONK;
    public static DeferredHolder<Item, AutoVanishBoatItem> AV_OAK_BOAT, AV_SPRUCE_BOAT, AV_BIRCH_BOAT, AV_JUNGLE_BOAT, AV_ACACIA_BOAT, AV_CHERRY_BOAT, AV_DARK_OAK_BOAT, AV_MANGROVE_BOAT, AV_BAMBOO_RAFT;
    public static DeferredHolder<Item, AutoVanishMinecartItem> AV_MINE_CART;
    public static DeferredHolder<Item, AccessControlToolItem> DISPLAY_MODE_TOOL;
    public static DeferredHolder<Item, AccessControlToolItem> STATIC_MODE_TOOL;
    public static DeferredHolder<Item, AccessControlToolItem> CACHED_MODE_TOOL;
    public static DeferredHolder<Item, TextureExtractor> TEXTURE_EXTRACTOR;

    public static Supplier<Item> MARTING_RED = ITEMS.register("marting_car_red", () -> new MartingCarItem(new Item.Properties(), MartingCarEntity.Variant.RED));
    public static Supplier<Item> MARTING_GREEN = ITEMS.register("marting_car_green", () -> new MartingCarItem(new Item.Properties(), MartingCarEntity.Variant.GREEN));
    public static Supplier<Item> MARTING_BLUE = ITEMS.register("marting_car_blue", () -> new MartingCarItem(new Item.Properties(), MartingCarEntity.Variant.BLUE));

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
        ITEMS.register("transparent_brush", TransparentBrushItem::new);
        ITEMS.register("examine_holo_glass", ExamineHoloGlass::new);
        ITEMS.register("command_rune", () -> new CommandRune(new Item.Properties()));
        TONK = ITEMS.register("tonk", () -> new TonkItem(new Item.Properties(), FenceKnotEntity.Type.Thin));
        THICK_TONK = ITEMS.register("thick_tonk", () -> new TonkItem(new Item.Properties(), FenceKnotEntity.Type.Normal));
        EXTRA_THICK_TONK = ITEMS.register("extra_thick_tonk", () -> new TonkItem(new Item.Properties(), FenceKnotEntity.Type.Thick));
        AV_OAK_BOAT = ITEMS.register("auto_vanish_oak_boat", () -> new AutoVanishBoatItem(Boat.Type.OAK));
        AV_SPRUCE_BOAT = ITEMS.register("auto_vanish_spruce_boat", () -> new AutoVanishBoatItem(Boat.Type.SPRUCE));
        AV_BIRCH_BOAT = ITEMS.register("auto_vanish_birch_boat", () -> new AutoVanishBoatItem(Boat.Type.BIRCH));
        AV_JUNGLE_BOAT = ITEMS.register("auto_vanish_jungle_boat", () -> new AutoVanishBoatItem(Boat.Type.JUNGLE));
        AV_ACACIA_BOAT = ITEMS.register("auto_vanish_acacia_boat", () -> new AutoVanishBoatItem(Boat.Type.ACACIA));
        AV_CHERRY_BOAT = ITEMS.register("auto_vanish_cherry_boat", () -> new AutoVanishBoatItem(Boat.Type.CHERRY));
        AV_DARK_OAK_BOAT = ITEMS.register("auto_vanish_dark_oak_boat", () -> new AutoVanishBoatItem(Boat.Type.DARK_OAK));
        AV_MANGROVE_BOAT = ITEMS.register("auto_vanish_mangrove_boat", () -> new AutoVanishBoatItem(Boat.Type.MANGROVE));
        AV_BAMBOO_RAFT = ITEMS.register("auto_vanish_bamboo_raft", () -> new AutoVanishBoatItem(Boat.Type.BAMBOO));
        AV_MINE_CART = ITEMS.register("auto_vanish_minecart", () -> new AutoVanishMinecartItem(new Item.Properties()));
        DISPLAY_MODE_TOOL = ITEMS.register("display_mode_tool", () -> new AccessControlToolItem(new Item.Properties(), AccessControlToolItem.Type.DISPLAY_MODE));
        STATIC_MODE_TOOL = ITEMS.register("static_mode_tool", () -> new AccessControlToolItem(new Item.Properties(), AccessControlToolItem.Type.STATIC_MODE));
        CACHED_MODE_TOOL = ITEMS.register("cached_mode_tool", () -> new AccessControlToolItem(new Item.Properties(), AccessControlToolItem.Type.CACHED_MODE));
        TEXTURE_EXTRACTOR = ITEMS.register("texture_extractor", () -> new TextureExtractor(new Item.Properties()));
        CREATIVE_MODE_TABS.register(bus);
        PowerToolDataComponents.DATA_COMPONENTS.register(bus);
    }

    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == THE_TAB.get()) {
            for (var regObj : ITEMS.getEntries()) {
                if (!(regObj.get() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof ICosmeticBlock))
                    event.accept(regObj.get());
            }
        }
        if (event.getTab() == COSMETIC_TAB.get()) {
            for (var regObj : ITEMS.getEntries()) {
                if (regObj.get() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ICosmeticBlock)
                    event.accept(regObj.get());
            }
        }
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            for (var regObj : ITEMS.getEntries()) {
                if (regObj.get() instanceof IRedStoneStuff || (regObj.get() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IRedStoneStuff))
                    event.accept(regObj.get());
            }
        }
    }
}
