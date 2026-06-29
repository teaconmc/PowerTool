package org.teacon.powertool.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
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
import org.teacon.powertool.entity.PowerToolEntities;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber(modid = PowerTool.MODID)
public class PowerToolItems {
    
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems( PowerTool.MODID);
    
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
    public static DeferredHolder<Item, ExamineHoloGlass> EXAMINE_HOLO_GLASS;
    public static DeferredHolder<Item, CommandRune> COMMAND_RUNE;

    public static DeferredHolder<Item, ExhibitionEntityEditor> EXHIBITION_ENTITY_EDITOR;
    public static DeferredHolder<Item, SpawnEggItem> REGULAR_EXHIBITION_HUMANOID;
    public static DeferredHolder<Item, SpawnEggItem> SLIM_EXHIBITION_HUMANOID;
    
    public static Supplier<Item> MARTING_RED = ITEMS.registerItem("marting_car_red", (p) -> new MartingCarItem(p, MartingCarEntity.Variant.RED));
    public static Supplier<Item> MARTING_GREEN = ITEMS.registerItem("marting_car_green", (p) -> new MartingCarItem(p, MartingCarEntity.Variant.GREEN));
    public static Supplier<Item> MARTING_BLUE = ITEMS.registerItem("marting_car_blue", (p) -> new MartingCarItem(p, MartingCarEntity.Variant.BLUE));


    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        ITEMS.registerItem("useless_stick", (p) -> new Item(p) {
            @Override
            public boolean isFoil(ItemStack stack) {
                return true;
            }
        });
        ITEMS.registerItem("clap", ClapItem::new);
        ITEMS.registerItem("clap_but_sad", ClapItem::new);
        ITEMS.registerItem("clap_but_angry", ClapItem::new);
        ITEMS.registerItem("transparent_brush", TransparentBrushItem::new);
        EXAMINE_HOLO_GLASS = ITEMS.registerItem("examine_holo_glass", ExamineHoloGlass::new);
        COMMAND_RUNE = ITEMS.registerItem("command_rune", CommandRune::new);
        TONK = ITEMS.registerItem("tonk", (p) -> new TonkItem(p, FenceKnotEntity.Type.Thin));
        THICK_TONK = ITEMS.registerItem("thick_tonk", (p) -> new TonkItem(p, FenceKnotEntity.Type.Normal));
        EXTRA_THICK_TONK = ITEMS.registerItem("extra_thick_tonk", (p) -> new TonkItem(p, FenceKnotEntity.Type.Thick));
        AV_OAK_BOAT = ITEMS.registerItem("auto_vanish_oak_boat", (p) -> new AutoVanishBoatItem(p,EntityType.OAK_BOAT));
        AV_SPRUCE_BOAT = ITEMS.registerItem("auto_vanish_spruce_boat", (p) -> new AutoVanishBoatItem(p,EntityType.SPRUCE_BOAT));
        AV_BIRCH_BOAT = ITEMS.registerItem("auto_vanish_birch_boat", (p) -> new AutoVanishBoatItem(p,EntityType.BIRCH_BOAT));
        AV_JUNGLE_BOAT = ITEMS.registerItem("auto_vanish_jungle_boat", (p) -> new AutoVanishBoatItem(p,EntityType.JUNGLE_BOAT));
        AV_ACACIA_BOAT = ITEMS.registerItem("auto_vanish_acacia_boat", (p) -> new AutoVanishBoatItem(p,EntityType.ACACIA_BOAT));
        AV_CHERRY_BOAT = ITEMS.registerItem("auto_vanish_cherry_boat", (p) -> new AutoVanishBoatItem(p,EntityType.CHERRY_BOAT));
        AV_DARK_OAK_BOAT = ITEMS.registerItem("auto_vanish_dark_oak_boat", (p) -> new AutoVanishBoatItem(p,EntityType.DARK_OAK_BOAT));
        AV_MANGROVE_BOAT = ITEMS.registerItem("auto_vanish_mangrove_boat", (p) -> new AutoVanishBoatItem(p,EntityType.MANGROVE_BOAT));
        AV_BAMBOO_RAFT = ITEMS.registerItem("auto_vanish_bamboo_raft", (p) -> new AutoVanishBoatItem(p,EntityType.BAMBOO_RAFT));
        AV_MINE_CART = ITEMS.registerItem("auto_vanish_minecart", AutoVanishMinecartItem::new);
        DISPLAY_MODE_TOOL = ITEMS.registerItem("display_mode_tool", (p) -> new AccessControlToolItem(p, AccessControlToolItem.Type.DISPLAY_MODE));
        STATIC_MODE_TOOL = ITEMS.registerItem("static_mode_tool", (p) -> new AccessControlToolItem(p, AccessControlToolItem.Type.STATIC_MODE));
        CACHED_MODE_TOOL = ITEMS.registerItem("cached_mode_tool", (p) -> new AccessControlToolItem(p, AccessControlToolItem.Type.CACHED_MODE));
        TEXTURE_EXTRACTOR = ITEMS.registerItem("texture_extractor", TextureExtractor::new);

        EXHIBITION_ENTITY_EDITOR    = ITEMS.registerItem("exhibition_entity_editor", ExhibitionEntityEditor::new);

        REGULAR_EXHIBITION_HUMANOID = ITEMS.registerItem("regular_exhibition_humanoid", (p) -> new SpawnEggItem(p.spawnEgg(PowerToolEntities.REGULAR_EXHIBITION_HUMANOID.get())));
        SLIM_EXHIBITION_HUMANOID    = ITEMS.registerItem("slim_exhibition_humanoid", (p) -> new SpawnEggItem(p.spawnEgg(PowerToolEntities.SLIM_EXHIBITION_HUMANOID.get())));

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
