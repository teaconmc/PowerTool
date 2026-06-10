package org.teacon.powertool.block;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.cosmetical.CosmeticBarrel;
import org.teacon.powertool.block.cosmetical.CosmeticBeehive;
import org.teacon.powertool.block.cosmetical.CosmeticCampfireBlock;
import org.teacon.powertool.block.cosmetical.CosmeticFurnace;
import org.teacon.powertool.block.cosmetical.CosmeticHopper;
import org.teacon.powertool.block.cosmetical.CosmeticHorizontalDirectionalBlock;
import org.teacon.powertool.block.cosmetical.CosmeticSimpleBlock;
import org.teacon.powertool.block.cosmetical.CosmeticTrapdoor;
import org.teacon.powertool.block.cosmetical.CosmeticWaterloggedBlock;
import org.teacon.powertool.block.entity.BezierCurveBlockEntity;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.ItemDisplayBlockEntity;
import org.teacon.powertool.block.entity.JEIRecipeDisplayBlockEntity;
import org.teacon.powertool.block.entity.ItemSupplierBlockEntity;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.block.entity.PowerSupplyBlockEntity;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.RedStoneDelayBlockEntity;
import org.teacon.powertool.block.entity.RegisterBlockEntity;
import org.teacon.powertool.block.entity.SafeBlockEntity;
import org.teacon.powertool.block.entity.TempleBlockEntity;
import org.teacon.powertool.block.entity.TimeObserverBlockEntity;
import org.teacon.powertool.block.entity.TrashCanWithContainerBlockEntity;
import org.teacon.powertool.block.fluid.FakeWater;
import org.teacon.powertool.block.holo_sign.HolographicSignBlock;
import org.teacon.powertool.block.holo_sign.SignType;
import org.teacon.powertool.item.PowerToolBlockItem;
import org.teacon.powertool.item.PowerToolDataComponents;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.teacon.powertool.item.PowerToolItems.ITEMS;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PowerToolBlocks {
    
    public static final SoundType ITEM_DISPLAY_SOUND_TYPE = new DeferredSoundType(1.0F, 1.0F,
            () -> SoundEvents.ITEM_FRAME_BREAK,
            () -> SoundEvents.MOSS_CARPET_STEP,
            () -> SoundEvents.ITEM_FRAME_PLACE,
            () -> SoundEvents.ITEM_FRAME_REMOVE_ITEM,
            () -> SoundEvents.MOSS_CARPET_FALL
    );
    
    public static final SoundType GLOW_ITEM_DISPLAY_SOUND_TYPE = new DeferredSoundType(1.0F, 1.0F,
            () -> SoundEvents.GLOW_ITEM_FRAME_BREAK,
            () -> SoundEvents.MOSS_CARPET_STEP,
            () -> SoundEvents.GLOW_ITEM_FRAME_PLACE,
            () -> SoundEvents.GLOW_ITEM_FRAME_REMOVE_ITEM,
            () -> SoundEvents.MOSS_CARPET_FALL);
    
    public static final List<Block> SIMPLE_BLOCKS = List.of(Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.CARTOGRAPHY_TABLE, Blocks.CRAFTING_TABLE, Blocks.FLETCHING_TABLE, Blocks.SMITHING_TABLE, Blocks.BEACON);
    
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PowerTool.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PowerTool.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, PowerTool.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, PowerTool.MODID);
    public static final Map<DyeColor, DeferredHolder<Block, ? extends Block>> DH_CHEAT_BLOCKS = new EnumMap<>(DyeColor.class);
    
    public static DeferredHolder<Block, PeriodicCommandBlock> COMMAND_BLOCK;
    public static DeferredHolder<Block, TrashCanWithContainer> TRASH_CAN;
    public static DeferredHolder<Block, PowerSupplyBlock> POWER_SUPPLY;
    public static DeferredHolder<Block, ItemDisplayBlock> ITEM_DISPLAY;
    
    public static DeferredHolder<Block, ItemDisplayBlock> GLOW_ITEM_DISPLAY;
    public static DeferredHolder<Block, ItemSupplierBlock> ITEM_SUPPLIER;
    
    public static DeferredHolder<Block, SlimItemSupplierBlock> SLIM_ITEM_SUPPLIER;
    public static DeferredHolder<Block, CosmeticHopper> COSMETIC_HOPPER;
    public static DeferredHolder<Block, CosmeticCampfireBlock> COSMETIC_CAMPFIRE;
    public static DeferredHolder<Block, CosmeticCampfireBlock> COSMETIC_SOUL_CAMPFIRE;
    public static DeferredHolder<Block, CosmeticBeehive> COSMETIC_BEEHIVE;
    public static DeferredHolder<Block, CosmeticFurnace> COSMETIC_FURNACE;
    public static DeferredHolder<Block, CosmeticFurnace> COSMETIC_BLAST_FURNACE;
    public static DeferredHolder<Block, CosmeticFurnace> COSMETIC_SMOKER;
    public static DeferredHolder<Block, CosmeticBarrel> COSMETIC_BARREL;
    
    public static DeferredHolder<Block, HolographicSignBlock> HOLOGRAPHIC_SIGN;
    public static DeferredHolder<Block, HolographicSignBlock> LINK_HOLOGRAPHIC_SIGN;
    public static DeferredHolder<Block, HolographicSignBlock> RAW_JSON_HOLOGRAPHIC_SIGN;
    public static DeferredHolder<Block, TrashCanWithContainer> WHITE_TRASH_CAN;
    public static DeferredHolder<Block, TrapDoorBlock> WHITE_TRASH_CAN_CAP;
    public static DeferredHolder<Block, TrashCanWithContainer> GRAY_TRASH_CAN;
    //public static DeferredHolder<Block, TrapDoorBlock> GRAY_TRASH_CAN_CAP;
    public static DeferredHolder<Block, TrashCanWithContainer> GREEN_TRASH_CAN;
    public static DeferredHolder<Block, TrapDoorBlock> GREEN_TRASH_CAN_CAP;
    
    public static DeferredHolder<Block, RegisterBlock> REGISTER;
    public static DeferredHolder<Block, RegisterBlock> GORGEOUS_REGISTER;
    public static DeferredHolder<Block, RegisterBlock> MECHANICAL_REGISTER;
    public static DeferredHolder<Block, RegisterBlock> TECH_REGISTER;
    
    public static DeferredHolder<Block, SafeBlock> SAFE;
    public static DeferredHolder<Block, SafeBlock> GORGEOUS_SAFE;
    public static DeferredHolder<Block, SafeBlock> MECHANICAL_SAFE;
    public static DeferredHolder<Block, SafeBlock> TECH_SAFE;
    public static DeferredHolder<Block, TempleBlock> TEMPLE;
    public static DeferredHolder<Block, TimeObserverBlock> REAL_TIME_OBSERVER;
    public static DeferredHolder<Block, TimeObserverBlock> REAL_TIME_CYCLE_OBSERVER;
    public static DeferredHolder<Block, TimeObserverBlock> GAME_TIME_CYCLE_OBSERVER;
    public static DeferredHolder<Block, RedStoneDelayBlock> DELAYER;
    public static DeferredHolder<Block, BezierCurveBlock> BEZIER_CURVE_BLOCK;
    public static DeferredHolder<Block, JEIRecipeDisplayBlock> JEI_RECIPE_DISPLAY_BLOCK;

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<PeriodicCommandBlockEntity>> COMMAND_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerSupplyBlockEntity>> POWER_SUPPLY_BLOCK_ENTITY;
    
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemDisplayBlockEntity>> ITEM_DISPLAY_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemSupplierBlockEntity>> ITEM_SUPPLIER_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<CommonHolographicSignBlockEntity>> HOLOGRAPHIC_SIGN_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<LinkHolographicSignBlockEntity>> LINK_HOLOGRAPHIC_SIGN_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<RawJsonHolographicSignBlockEntity>> RAW_JSON_HOLOGRAPHIC_SIGN_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TrashCanWithContainerBlockEntity>> TRASH_CAN_WITH_CONTAINER_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<RegisterBlockEntity>> REGISTER_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SafeBlockEntity>> SAFE_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TempleBlockEntity>> TEMPLE_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TimeObserverBlockEntity>> TIME_OBSERVER_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<RedStoneDelayBlockEntity>> DELAYER_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BezierCurveBlockEntity>> BEZIER_CURVE_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<JEIRecipeDisplayBlockEntity>> JEI_RECIPE_DISPLAY_BLOCK_ENTITY;

    public static DeferredHolder<Fluid, FakeWater> FAKE_WATER = FLUIDS.register("fake_water", FakeWater::new);
    public static DeferredHolder<Block, LiquidBlock> FAKE_WATER_BLOCK = BLOCKS.registerBlock("fake_water_block", (p) -> new LiquidBlock(
            FAKE_WATER.get(), p),() -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER));
    public static DeferredHolder<Item, BucketItem> FAKE_WATER_BUCKET = ITEMS.registerItem("fake_water_bucket", (p) -> new BucketItem(FAKE_WATER.get(), p.craftRemainder(Items.BUCKET).stacksTo(1)));
    public static DeferredHolder<FluidType, FluidType> FAKE_WATER_TYPE = FLUID_TYPES.register("fake_water", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.powertool.fake_water")
            .fallDistanceModifier(0F)
            .canExtinguish(true)
            .canConvertToSource(true)
            .supportsBoating(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
            .canHydrate(true)
            .addDripstoneDripping(PointedDripstoneBlock.WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK, ParticleTypes.DRIPPING_DRIPSTONE_WATER, Blocks.WATER_CAULDRON, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON)
    ));
    
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        FLUIDS.register(bus);
        FLUID_TYPES.register(bus);
        COMMAND_BLOCK = BLOCKS.registerBlock("command_block", (p) -> new PeriodicCommandBlock(
                p.mapColor(DyeColor.PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable(),
                false
        ));
        TRASH_CAN = BLOCKS.registerBlock("trash_can", (p) -> new TrashCanWithContainer(p.strength(1000).noOcclusion()));
        POWER_SUPPLY = BLOCKS.registerBlock("power_supply", (p) -> new PowerSupplyBlock(p.strength(1000)),() -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
        ITEM_DISPLAY = BLOCKS.registerBlock("item_display", (p) -> new ItemDisplayBlock(p.sound(ITEM_DISPLAY_SOUND_TYPE).noOcclusion().strength(10000)),() -> BlockBehaviour.Properties.ofFullCopy(Blocks.LADDER));
        GLOW_ITEM_DISPLAY = BLOCKS.registerBlock("glow_item_display", (p) -> new ItemDisplayBlock(p.sound(GLOW_ITEM_DISPLAY_SOUND_TYPE).noOcclusion().strength(10000)),() -> BlockBehaviour.Properties.ofFullCopy(Blocks.LADDER));
        ITEM_SUPPLIER = BLOCKS.registerBlock("item_supplier", (p) -> new ItemSupplierBlock(p.strength(1000).noOcclusion()));
        SLIM_ITEM_SUPPLIER = BLOCKS.registerBlock("slim_item_supplier", (p) -> new SlimItemSupplierBlock(p.strength(1000).noOcclusion()));
        COSMETIC_HOPPER = BLOCKS.registerBlock("cosmetic_hopper", CosmeticHopper::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER));
        COSMETIC_CAMPFIRE = BLOCKS.registerBlock("cosmetic_campfire", (p) -> new CosmeticCampfireBlock(true, p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE));
        COSMETIC_SOUL_CAMPFIRE = BLOCKS.registerBlock("cosmetic_soul_campfire", (p) -> new CosmeticCampfireBlock(false, p),() -> BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_CAMPFIRE));
        HOLOGRAPHIC_SIGN = BLOCKS.registerBlock("holographic_sign", (p) -> new HolographicSignBlock(p.noCollision().noLootTable(), SignType.COMMON));
        LINK_HOLOGRAPHIC_SIGN = BLOCKS.registerBlock("link_holographic_sign", (p) -> new HolographicSignBlock(p.noCollision().noLootTable(), SignType.URL));
        RAW_JSON_HOLOGRAPHIC_SIGN = BLOCKS.registerBlock("raw_json_holographic_sign", (p) -> new HolographicSignBlock(p.noCollision().noLootTable(), SignType.RAW_JSON));
        WHITE_TRASH_CAN = BLOCKS.registerBlock("white_trash_can", (p) -> new TrashCanWithContainer(p.noOcclusion()));
        WHITE_TRASH_CAN_CAP = BLOCKS.registerBlock("white_trash_can_cap", (p) -> new TrapDoorBlock(BlockSetType.COPPER, p.noOcclusion()));
        GRAY_TRASH_CAN = BLOCKS.registerBlock("gray_trash_can", (p) -> new TrashCanWithContainer(p.noOcclusion()));
        //GRAY_TRASH_CAN_CAP = BLOCKS.registerBlock("gray_trash_can_cap",() -> new TrapDoorBlock(BlockSetType.IRON, BlockBehaviour.Properties.of().noOcclusion()));
        GREEN_TRASH_CAN = BLOCKS.registerBlock("green_trash_can", (p) -> new TrashCanWithContainer(p.noOcclusion()));
        GREEN_TRASH_CAN_CAP = BLOCKS.registerBlock("green_trash_can_cap", (p) -> new TrapDoorBlock(BlockSetType.COPPER, p.noOcclusion()));
        
        REGISTER = BLOCKS.registerBlock("register", (p) -> new RegisterBlock(p.noOcclusion()));
        GORGEOUS_REGISTER = BLOCKS.registerBlock("gorgeous_register", (p) -> new RegisterBlock(p.noOcclusion()));
        MECHANICAL_REGISTER = BLOCKS.registerBlock("mechanical_register", (p) -> new RegisterBlock(p.noOcclusion()));
        TECH_REGISTER = BLOCKS.registerBlock("tech_register", (p) -> new RegisterBlock(p.noOcclusion()));
        
        SAFE = BLOCKS.registerBlock("safe", (p) -> new SafeBlock(p.strength(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)));
        GORGEOUS_SAFE = BLOCKS.registerBlock("gorgeous_safe", (p) -> new SafeBlock(p.strength(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)));
        MECHANICAL_SAFE = BLOCKS.registerBlock("mechanical_safe", (p) -> new SafeBlock(p.strength(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)));
        TECH_SAFE = BLOCKS.registerBlock("tech_safe", (p) -> new SafeBlock(p.strength(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)));
        TEMPLE = BLOCKS.registerBlock("temple", (p) -> new TempleBlock(p.noOcclusion()));
        
        REAL_TIME_OBSERVER = BLOCKS.registerBlock("observer_realtime", (p) -> new TimeObserverBlock(p, TimeObserverBlock.Type.REAL_TIME));
        REAL_TIME_CYCLE_OBSERVER = BLOCKS.registerBlock("observer_realtime_cyl", (p) -> new TimeObserverBlock(p, TimeObserverBlock.Type.REAL_DAILY_CYCLE));
        GAME_TIME_CYCLE_OBSERVER = BLOCKS.registerBlock("observer_gametime_cyl", (p) -> new TimeObserverBlock(p, TimeObserverBlock.Type.GAME_DAILY_CYCLE));
        DELAYER = BLOCKS.registerBlock("delayer", RedStoneDelayBlock::new);
        BEZIER_CURVE_BLOCK = BLOCKS.registerBlock("bezier_curve_block", (p) -> new BezierCurveBlock(p.noOcclusion()));
        JEI_RECIPE_DISPLAY_BLOCK = BLOCKS.registerBlock("jei_recipe_display_block", (p) -> new JEIRecipeDisplayBlock(p.noOcclusion()));

        for (var dyeColor : DyeColor.values()) {
            var name = dyeColor.getName() + "_distant_horizon_cheating_block";
            var block = BLOCKS.registerBlock(name, (p) -> new DistantHorizonCheatingBlock(p.forceSolidOn().noOcclusion().mapColor(dyeColor)));
            DH_CHEAT_BLOCKS.put(dyeColor, block);
            ITEMS.registerItem(name, (p) -> new PowerToolBlockItem(block.get(), p));
        }
        
        COMMAND_BLOCK_ENTITY = BLOCK_ENTITIES.register("command_block_entity", () -> new BlockEntityType<>(
                PeriodicCommandBlockEntity::new, COMMAND_BLOCK.get()
        ));
        POWER_SUPPLY_BLOCK_ENTITY = BLOCK_ENTITIES.register("power_supply", () -> new BlockEntityType<>(
                PowerSupplyBlockEntity::new, POWER_SUPPLY.get()
        ));
        ITEM_DISPLAY_BLOCK_ENTITY = BLOCK_ENTITIES.register("item_display", () -> new BlockEntityType<>(
                ItemDisplayBlockEntity::new, ITEM_DISPLAY.get(), GLOW_ITEM_DISPLAY.get()
        ));
        ITEM_SUPPLIER_BLOCK_ENTITY = BLOCK_ENTITIES.register("item_supplier", () -> new BlockEntityType<>(
                ItemSupplierBlockEntity::new, ITEM_SUPPLIER.get(), SLIM_ITEM_SUPPLIER.get()
        ));
        HOLOGRAPHIC_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("holographic_sign", () -> new BlockEntityType<>(
                CommonHolographicSignBlockEntity::new, HOLOGRAPHIC_SIGN.get()
        ));
        LINK_HOLOGRAPHIC_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("link_holographic_sign", () -> new BlockEntityType<>(
                LinkHolographicSignBlockEntity::new, LINK_HOLOGRAPHIC_SIGN.get()
        ));
        RAW_JSON_HOLOGRAPHIC_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("raw_json_holographic_sign", () -> new BlockEntityType<>(
                RawJsonHolographicSignBlockEntity::new, RAW_JSON_HOLOGRAPHIC_SIGN.get()
        ));
        TRASH_CAN_WITH_CONTAINER_BLOCK_ENTITY = BLOCK_ENTITIES.register("trash_can_with_container", () -> new BlockEntityType<>(
                TrashCanWithContainerBlockEntity::new, TRASH_CAN.get(), WHITE_TRASH_CAN.get(), GRAY_TRASH_CAN.get(), GREEN_TRASH_CAN.get()
        ));
        REGISTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("register", () -> new BlockEntityType<>(
                RegisterBlockEntity::new, REGISTER.get(), GORGEOUS_REGISTER.get(), MECHANICAL_REGISTER.get(), TECH_REGISTER.get()
        ));
        SAFE_BLOCK_ENTITY = BLOCK_ENTITIES.register("safe", () -> new BlockEntityType<>(
                SafeBlockEntity::new, SAFE.get(), GORGEOUS_SAFE.get(), MECHANICAL_SAFE.get(), TECH_SAFE.get()
        ));
        TEMPLE_BLOCK_ENTITY = BLOCK_ENTITIES.register("temple", () -> new BlockEntityType<>(
                TempleBlockEntity::new, TEMPLE.get()
        ));
        TIME_OBSERVER_BLOCK_ENTITY = BLOCK_ENTITIES.register("time_observer", () -> new BlockEntityType<>(
                TimeObserverBlockEntity::new, REAL_TIME_OBSERVER.get(), REAL_TIME_CYCLE_OBSERVER.get(), GAME_TIME_CYCLE_OBSERVER.get()
        ));
        DELAYER_BLOCK_ENTITY = BLOCK_ENTITIES.register("delayer", () -> new BlockEntityType<>(
                RedStoneDelayBlockEntity::new, DELAYER.get()
        ));
        BEZIER_CURVE_BLOCK_ENTITY = BLOCK_ENTITIES.register("bezier_curve_block", () -> new BlockEntityType<>(
                BezierCurveBlockEntity::new, BEZIER_CURVE_BLOCK.get()
        ));
        JEI_RECIPE_DISPLAY_BLOCK_ENTITY = BLOCK_ENTITIES.register("jei_recipe_display_block", () -> new BlockEntityType<>(
                JEIRecipeDisplayBlockEntity::new, JEI_RECIPE_DISPLAY_BLOCK.get()
        ));

        regTrapDoors(Map.of(
                BlockSetType.OAK, Blocks.OAK_TRAPDOOR,
                BlockSetType.BIRCH, Blocks.BIRCH_TRAPDOOR,
                BlockSetType.SPRUCE, Blocks.SPRUCE_TRAPDOOR,
                BlockSetType.JUNGLE, Blocks.JUNGLE_TRAPDOOR,
                BlockSetType.ACACIA, Blocks.ACACIA_TRAPDOOR,
                BlockSetType.DARK_OAK, Blocks.DARK_OAK_TRAPDOOR,
                BlockSetType.CRIMSON, Blocks.CRIMSON_TRAPDOOR,
                BlockSetType.WARPED, Blocks.WARPED_TRAPDOOR,
                BlockSetType.BAMBOO, Blocks.BAMBOO_TRAPDOOR,
                BlockSetType.MANGROVE, Blocks.MANGROVE_TRAPDOOR
        ));
        regTrapDoors(Map.of(
                BlockSetType.CHERRY, Blocks.CHERRY_TRAPDOOR,
                BlockSetType.IRON, Blocks.IRON_TRAPDOOR
        ));
        
        regCosmetic(SIMPLE_BLOCKS, CosmeticSimpleBlock::new);
        regCosmetic(List.of(Blocks.ENCHANTING_TABLE), CosmeticWaterloggedBlock::new);
        regCosmetic(List.of(Blocks.STONECUTTER), CosmeticHorizontalDirectionalBlock::new);
        
        COSMETIC_BEEHIVE = BLOCKS.registerBlock("cosmetic_beehive", CosmeticBeehive::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.BEEHIVE));
        COSMETIC_FURNACE = BLOCKS.registerBlock("cosmetic_furnace", CosmeticFurnace::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE));
        COSMETIC_BLAST_FURNACE = BLOCKS.registerBlock("cosmetic_blast_furnace", CosmeticFurnace::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE));
        COSMETIC_SMOKER = BLOCKS.registerBlock("cosmetic_smoker", CosmeticFurnace::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SMOKER));
        COSMETIC_BARREL = BLOCKS.registerBlock("cosmetic_barrel", CosmeticBarrel::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL));
        
        ITEMS.registerItem("cosmetic_beehive", (p) -> new PowerToolBlockItem(COSMETIC_BEEHIVE.get(), p));
        ITEMS.registerItem("cosmetic_furnace", (p) -> new PowerToolBlockItem(COSMETIC_FURNACE.get(), p));
        ITEMS.registerItem("cosmetic_blast_furnace", (p) -> new PowerToolBlockItem(COSMETIC_BLAST_FURNACE.get(), p));
        ITEMS.registerItem("cosmetic_smoker", (p) -> new PowerToolBlockItem(COSMETIC_SMOKER.get(), p));
        ITEMS.registerItem("cosmetic_barrel", (p) -> new PowerToolBlockItem(COSMETIC_BARREL.get(), p));
        
        ITEMS.registerItem("command_block", (p) -> new PowerToolBlockItem(COMMAND_BLOCK.get(), p.rarity(Rarity.EPIC)));
        ITEMS.registerItem("power_supply", (p) -> new PowerToolBlockItem(POWER_SUPPLY.get(), p));
        ITEMS.registerItem("item_display", (p) -> new PowerToolBlockItem(ITEM_DISPLAY.get(), p));
        ITEMS.registerItem("glow_item_display", (p) -> new PowerToolBlockItem(GLOW_ITEM_DISPLAY.get(), p));
        ITEMS.registerItem("slim_item_supplier", (p) -> new PowerToolBlockItem(SLIM_ITEM_SUPPLIER.get(), p));
        ITEMS.registerItem("item_supplier", (p) -> new PowerToolBlockItem(ITEM_SUPPLIER.get(), p));
        ITEMS.registerItem("cosmetic_hopper", (p) -> new PowerToolBlockItem(COSMETIC_HOPPER.get(), p));
        ITEMS.registerItem("cosmetic_campfire", (p) -> new PowerToolBlockItem(COSMETIC_CAMPFIRE.get(), p));
        ITEMS.registerItem("cosmetic_soul_campfire", (p) -> new PowerToolBlockItem(COSMETIC_SOUL_CAMPFIRE.get(), p));
        ITEMS.registerItem("holographic_sign", (p) -> new PowerToolBlockItem(HOLOGRAPHIC_SIGN.get(), p));
        ITEMS.registerItem("link_holographic_sign", (p) -> new PowerToolBlockItem(LINK_HOLOGRAPHIC_SIGN.get(), p));
        ITEMS.registerItem("raw_json_holographic_sign", (p) -> new PowerToolBlockItem(RAW_JSON_HOLOGRAPHIC_SIGN.get(), p));
        ITEMS.registerItem("trash_can", (p) -> new PowerToolBlockItem(TRASH_CAN.get(), p));
        ITEMS.registerItem("white_trash_can", (p) -> new PowerToolBlockItem(WHITE_TRASH_CAN.get(), p));
        ITEMS.registerItem("white_trash_can_cap", (p) -> new PowerToolBlockItem(WHITE_TRASH_CAN_CAP.get(), p));
        ITEMS.registerItem("gray_trash_can", (p) -> new PowerToolBlockItem(GRAY_TRASH_CAN.get(), p));
        //ITEMS.register("gray_trash_can_cap",() -> new PowerToolBlockItem(GRAY_TRASH_CAN_CAP.get(),p));
        ITEMS.registerItem("green_trash_can", (p) -> new PowerToolBlockItem(GREEN_TRASH_CAN.get(), p));
        ITEMS.registerItem("green_trash_can_cap", (p) -> new PowerToolBlockItem(GREEN_TRASH_CAN_CAP.get(), p));
        
        ITEMS.registerItem("register", (p) -> new PowerToolBlockItem(REGISTER.get(), p));
        ITEMS.registerItem("gorgeous_register", (p) -> new PowerToolBlockItem(GORGEOUS_REGISTER.get(), p));
        ITEMS.registerItem("mechanical_register", (p) -> new PowerToolBlockItem(MECHANICAL_REGISTER.get(), p));
        ITEMS.registerItem("tech_register", (p) -> new PowerToolBlockItem(TECH_REGISTER.get(), p));
        ITEMS.registerItem("temple", (p) -> new PowerToolBlockItem(TEMPLE.get(), p));
        ITEMS.registerItem("safe", (p) -> new PowerToolBlockItem(SAFE.get(), p.component(PowerToolDataComponents.COMMAND, "/ac safe")));
        ITEMS.registerItem("gorgeous_safe", (p) -> new PowerToolBlockItem(GORGEOUS_SAFE.get(), p.component(PowerToolDataComponents.COMMAND, "/ac safe")));
        ITEMS.registerItem("mechanical_safe", (p) -> new PowerToolBlockItem(MECHANICAL_SAFE.get(), p.component(PowerToolDataComponents.COMMAND, "/ac safe")));
        ITEMS.registerItem("tech_safe", (p) -> new PowerToolBlockItem(TECH_SAFE.get(), p.component(PowerToolDataComponents.COMMAND, "/ac safe")));
        ITEMS.registerItem("observer_realtime", (p) -> new PowerToolBlockItem(REAL_TIME_OBSERVER.get(), p));
        ITEMS.registerItem("observer_realtime_cyl", (p) -> new PowerToolBlockItem(REAL_TIME_CYCLE_OBSERVER.get(), p));
        ITEMS.registerItem("observer_gametime_cyl", (p) -> new PowerToolBlockItem(GAME_TIME_CYCLE_OBSERVER.get(), p));
        ITEMS.registerItem("delayer", (p) -> new PowerToolBlockItem(DELAYER.get(), p));
        ITEMS.registerItem("bezier_curve_block", (p) -> new PowerToolBlockItem(BEZIER_CURVE_BLOCK.get(), p));
        ITEMS.registerItem("jei_recipe_display_block", (p) -> new PowerToolBlockItem(JEI_RECIPE_DISPLAY_BLOCK.get(), p));
    }

    private static void regTrapDoors(Map<BlockSetType, Block> existing) {
        for (var type : existing.entrySet()) {
            var name = "cosmetic_" + type.getKey().name() + "_trapdoor";
            var block = BLOCKS.registerBlock(name, CosmeticTrapdoor::new,() -> BlockBehaviour.Properties.ofFullCopy(type.getValue()));
            ITEMS.registerItem(name, (p) -> new PowerToolBlockItem(block.get(), p));
        }
    }
    
    private static void regCosmetic(List<Block> existing, Function<BlockBehaviour.Properties, Block> factory) {
        for (var existingBlock : existing) {
            var name = "cosmetic_" + BuiltInRegistries.BLOCK.getKey(existingBlock).getPath();
            var flag = existingBlock.defaultBlockState().useShapeForLightOcclusion();
            var properties = BlockBehaviour.Properties.ofFullCopy(existingBlock);
            if (flag) properties.noOcclusion();
            var block = BLOCKS.registerBlock(name, factory, () -> properties);
            ITEMS.registerItem(name, (p) -> new PowerToolBlockItem(block.get(), p));
        }
    }
    
    @SubscribeEvent
    public static void regBlockCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ITEM_SUPPLIER_BLOCK_ENTITY.get(),
                (be, context) -> be.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                POWER_SUPPLY_BLOCK_ENTITY.get(),
                (be, context) -> be.getEnergyStore()
        );
    }
}
