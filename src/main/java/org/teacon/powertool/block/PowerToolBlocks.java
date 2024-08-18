package org.teacon.powertool.block;

import com.mojang.datafixers.DSL;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.cosmetical.CosmeticBarrel;
import org.teacon.powertool.block.cosmetical.CosmeticBeehive;
import org.teacon.powertool.block.cosmetical.CosmeticCampfireBlock;
import org.teacon.powertool.block.cosmetical.CosmeticFurnace;
import org.teacon.powertool.block.cosmetical.CosmeticHopper;
import org.teacon.powertool.block.cosmetical.CosmeticSimpleBlock;
import org.teacon.powertool.block.cosmetical.CosmeticTrapdoor;
import org.teacon.powertool.block.entity.CommonHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.ItemDisplayBlockEntity;
import org.teacon.powertool.block.entity.ItemSupplierBlockEntity;
import org.teacon.powertool.block.entity.LinkHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.block.entity.PowerSupplyBlockEntity;
import org.teacon.powertool.block.entity.RawJsonHolographicSignBlockEntity;
import org.teacon.powertool.block.entity.RegisterBlockEntity;
import org.teacon.powertool.block.entity.TrashCanWithContainerBlockEntity;
import org.teacon.powertool.block.holo_sign.HolographicSignBlock;
import org.teacon.powertool.block.holo_sign.SignType;

import java.util.List;
import java.util.Map;

import static org.teacon.powertool.item.PowerToolItems.ITEMS;

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

    public static final List<Block> SIMPLE_BLOCKS = List.of(Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK, Blocks.REDSTONE_BLOCK);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, PowerTool.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PowerTool.MODID);

    public static DeferredHolder<Block,PeriodicCommandBlock> COMMAND_BLOCK;
    public static DeferredHolder<Block,TrashCanBlock> TRASH_CAN;
    public static DeferredHolder<Block,PowerSupplyBlock> POWER_SUPPLY;
    public static DeferredHolder<Block,ItemDisplayBlock> ITEM_DISPLAY;

    public static DeferredHolder<Block,ItemDisplayBlock> GLOW_ITEM_DISPLAY;
    public static DeferredHolder<Block,ItemSupplierBlock> ITEM_SUPPLIER;

    public static DeferredHolder<Block,SlimItemSupplierBlock> SLIM_ITEM_SUPPLIER;
    public static DeferredHolder<Block, CosmeticHopper> COSMETIC_HOPPER;
    public static DeferredHolder<Block, CosmeticCampfireBlock> COSMETIC_CAMPFIRE;
    public static DeferredHolder<Block,CosmeticCampfireBlock> COSMETIC_SOUL_CAMPFIRE;
    public static DeferredHolder<Block, CosmeticBeehive> COSMETIC_BEEHIVE;
    public static DeferredHolder<Block, CosmeticFurnace> COSMETIC_FURNACE;
    public static DeferredHolder<Block,CosmeticFurnace> COSMETIC_BLAST_FURNACE;
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

    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<PeriodicCommandBlockEntity>> COMMAND_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<PowerSupplyBlockEntity>> POWER_SUPPLY_BLOCK_ENTITY;

    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<ItemDisplayBlockEntity>> ITEM_DISPLAY_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<ItemSupplierBlockEntity>> ITEM_SUPPLIER_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<CommonHolographicSignBlockEntity>> HOLOGRAPHIC_SIGN_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<LinkHolographicSignBlockEntity>> LINK_HOLOGRAPHIC_SIGN_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<RawJsonHolographicSignBlockEntity>> RAW_JSON_HOLOGRAPHIC_SIGN_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<TrashCanWithContainerBlockEntity>> TRASH_CAN_WITH_CONTAINER_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<RegisterBlockEntity>> REGISTER_BLOCK_ENTITY;

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        COMMAND_BLOCK = BLOCKS.register("command_block", () -> new PeriodicCommandBlock(
            BlockBehaviour.Properties.of().mapColor(DyeColor.PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable(),
            false
        ));
        TRASH_CAN = BLOCKS.register("trash_can", () -> new TrashCanBlock(BlockBehaviour.Properties.of().strength(1000).noOcclusion()));
        POWER_SUPPLY = BLOCKS.register("power_supply", () -> new PowerSupplyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(1000)));
        ITEM_DISPLAY = BLOCKS.register("item_display", () -> new ItemDisplayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LADDER).sound(ITEM_DISPLAY_SOUND_TYPE).noOcclusion().strength(10000)));
        GLOW_ITEM_DISPLAY = BLOCKS.register("glow_item_display", () -> new ItemDisplayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LADDER).sound(GLOW_ITEM_DISPLAY_SOUND_TYPE).noOcclusion().strength(10000)));
        ITEM_SUPPLIER = BLOCKS.register("item_supplier", () -> new ItemSupplierBlock(BlockBehaviour.Properties.of().strength(1000).noOcclusion()));
        SLIM_ITEM_SUPPLIER = BLOCKS.register("slim_item_supplier", () -> new SlimItemSupplierBlock(BlockBehaviour.Properties.of().strength(1000).noOcclusion()));
        COSMETIC_HOPPER = BLOCKS.register("cosmetic_hopper", () -> new CosmeticHopper(BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER)));
        COSMETIC_CAMPFIRE = BLOCKS.register("cosmetic_campfire", () -> new CosmeticCampfireBlock(true, BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE)));
        COSMETIC_SOUL_CAMPFIRE = BLOCKS.register("cosmetic_soul_campfire", () -> new CosmeticCampfireBlock(false, BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_CAMPFIRE)));
        HOLOGRAPHIC_SIGN = BLOCKS.register("holographic_sign",
                () -> new HolographicSignBlock(
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT).lightLevel(state -> 15).noCollission().noLootTable(),
                        SignType.COMMON));
        LINK_HOLOGRAPHIC_SIGN = BLOCKS.register("link_holographic_sign",
                () -> new HolographicSignBlock(
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT).lightLevel(state -> 15).noCollission().noLootTable(),
                        SignType.URL));
        RAW_JSON_HOLOGRAPHIC_SIGN = BLOCKS.register("raw_json_holographic_sign",
                () -> new HolographicSignBlock(
                        BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT).lightLevel(state -> 15).noCollission().noLootTable(),
                        SignType.RAW_JSON));
        WHITE_TRASH_CAN = BLOCKS.register("white_trash_can", () -> new TrashCanWithContainer(BlockBehaviour.Properties.of().noOcclusion()));
        WHITE_TRASH_CAN_CAP = BLOCKS.register("white_trash_can_cap",() -> new TrapDoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.of().noOcclusion()));
        GRAY_TRASH_CAN = BLOCKS.register("gray_trash_can",() -> new TrashCanWithContainer(BlockBehaviour.Properties.of().noOcclusion()));
        //GRAY_TRASH_CAN_CAP = BLOCKS.register("gray_trash_can_cap",() -> new TrapDoorBlock(BlockSetType.IRON, BlockBehaviour.Properties.of().noOcclusion()));
        GREEN_TRASH_CAN = BLOCKS.register("green_trash_can",() -> new TrashCanWithContainer(BlockBehaviour.Properties.of().noOcclusion()));
        GREEN_TRASH_CAN_CAP = BLOCKS.register("green_trash_can_cap",() -> new TrapDoorBlock(BlockSetType.COPPER, BlockBehaviour.Properties.of().noOcclusion()));

        REGISTER = BLOCKS.register("register", () -> new RegisterBlock(BlockBehaviour.Properties.of().noOcclusion()));
        GORGEOUS_REGISTER = BLOCKS.register("gorgeous_register", () -> new RegisterBlock(BlockBehaviour.Properties.of().noOcclusion()));
        MECHANICAL_REGISTER = BLOCKS.register("mechanical_register", () -> new RegisterBlock(BlockBehaviour.Properties.of().noOcclusion()));
        TECH_REGISTER = BLOCKS.register("tech_register", () -> new RegisterBlock(BlockBehaviour.Properties.of().noOcclusion()));

        COMMAND_BLOCK_ENTITY = BLOCK_ENTITIES.register("command_block_entity", () -> BlockEntityType.Builder.of(
            PeriodicCommandBlockEntity::new, COMMAND_BLOCK.get()
        ).build(DSL.remainderType()));
        POWER_SUPPLY_BLOCK_ENTITY = BLOCK_ENTITIES.register("power_supply", () -> BlockEntityType.Builder.of(
                PowerSupplyBlockEntity::new, POWER_SUPPLY.get()
        ).build(DSL.remainderType()));
        ITEM_DISPLAY_BLOCK_ENTITY = BLOCK_ENTITIES.register("item_display", () -> BlockEntityType.Builder.of(
                ItemDisplayBlockEntity::new, ITEM_DISPLAY.get(), GLOW_ITEM_DISPLAY.get()
        ).build(DSL.remainderType()));
        ITEM_SUPPLIER_BLOCK_ENTITY = BLOCK_ENTITIES.register("item_supplier", () -> BlockEntityType.Builder.of(
                ItemSupplierBlockEntity::new, ITEM_SUPPLIER.get(), SLIM_ITEM_SUPPLIER.get()
        ).build(DSL.remainderType()));
        HOLOGRAPHIC_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("holographic_sign", () -> BlockEntityType.Builder.of(
                CommonHolographicSignBlockEntity::new, HOLOGRAPHIC_SIGN.get()
        ).build(DSL.remainderType()));
        LINK_HOLOGRAPHIC_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("link_holographic_sign",() -> BlockEntityType.Builder.of(
                LinkHolographicSignBlockEntity::new,LINK_HOLOGRAPHIC_SIGN.get()
        ).build(DSL.remainderType()));
        RAW_JSON_HOLOGRAPHIC_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("raw_json_holographic_sign",() -> BlockEntityType.Builder.of(
                RawJsonHolographicSignBlockEntity::new,RAW_JSON_HOLOGRAPHIC_SIGN.get()
        ).build(DSL.remainderType()));
        TRASH_CAN_WITH_CONTAINER_BLOCK_ENTITY = BLOCK_ENTITIES.register("trash_can_with_container",() -> BlockEntityType.Builder.of(
                TrashCanWithContainerBlockEntity::new,WHITE_TRASH_CAN.get(),GRAY_TRASH_CAN.get(),GREEN_TRASH_CAN.get()
        ).build(DSL.remainderType()));
        REGISTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("register", () -> BlockEntityType.Builder.of(
                RegisterBlockEntity::new, REGISTER.get(), GORGEOUS_REGISTER.get(), MECHANICAL_REGISTER.get(), TECH_REGISTER.get()
        ).build(DSL.remainderType()));

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

        regSimpleCosmetic(SIMPLE_BLOCKS);
        COSMETIC_BEEHIVE = BLOCKS.register("cosmetic_beehive", () -> new CosmeticBeehive(BlockBehaviour.Properties.ofFullCopy(Blocks.BEEHIVE)));
        COSMETIC_FURNACE = BLOCKS.register("cosmetic_furnace", () -> new CosmeticFurnace(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE)));
        COSMETIC_BLAST_FURNACE = BLOCKS.register("cosmetic_blast_furnace", () -> new CosmeticFurnace(BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE)));
        COSMETIC_BARREL = BLOCKS.register("cosmetic_barrel", () -> new CosmeticBarrel(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL)));

        ITEMS.register("cosmetic_beehive", () -> new BlockItem(COSMETIC_BEEHIVE.get(), new Item.Properties()));
        ITEMS.register("cosmetic_furnace", () -> new BlockItem(COSMETIC_FURNACE.get(), new Item.Properties()));
        ITEMS.register("cosmetic_blast_furnace", () -> new BlockItem(COSMETIC_BLAST_FURNACE.get(), new Item.Properties()));
        ITEMS.register("cosmetic_barrel", () -> new BlockItem(COSMETIC_BARREL.get(), new Item.Properties()));

        ITEMS.register("command_block", () -> new BlockItem(COMMAND_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
        ITEMS.register("trash_can", () -> new BlockItem(TRASH_CAN.get(), new Item.Properties()));
        ITEMS.register("power_supply", () -> new BlockItem(POWER_SUPPLY.get(), new Item.Properties()));
        ITEMS.register("item_display", () -> new BlockItem(ITEM_DISPLAY.get(), new Item.Properties()));
        ITEMS.register("glow_item_display", () -> new BlockItem(GLOW_ITEM_DISPLAY.get(), new Item.Properties()));
        ITEMS.register("slim_item_supplier", () -> new BlockItem(SLIM_ITEM_SUPPLIER.get(), new Item.Properties()));
        ITEMS.register("item_supplier", () -> new BlockItem(ITEM_SUPPLIER.get(), new Item.Properties()));
        ITEMS.register("cosmetic_hopper", () -> new BlockItem(COSMETIC_HOPPER.get(), new Item.Properties()));
        ITEMS.register("cosmetic_campfire", () -> new BlockItem(COSMETIC_CAMPFIRE.get(), new Item.Properties()));
        ITEMS.register("cosmetic_soul_campfire", () -> new BlockItem(COSMETIC_SOUL_CAMPFIRE.get(), new Item.Properties()));
        ITEMS.register("holographic_sign", () -> new BlockItem(HOLOGRAPHIC_SIGN.get(), new Item.Properties()));
        ITEMS.register("link_holographic_sign",() -> new BlockItem(LINK_HOLOGRAPHIC_SIGN.get(), new Item.Properties()));
        ITEMS.register("raw_json_holographic_sign",() -> new BlockItem(RAW_JSON_HOLOGRAPHIC_SIGN.get(), new Item.Properties()));
        ITEMS.register("white_trash_can",() -> new BlockItem(WHITE_TRASH_CAN.get(),new Item.Properties()));
        ITEMS.register("white_trash_can_cap",() -> new BlockItem(WHITE_TRASH_CAN_CAP.get(),new Item.Properties()));
        ITEMS.register("gray_trash_can",() -> new BlockItem(GRAY_TRASH_CAN.get(),new Item.Properties()));
        //ITEMS.register("gray_trash_can_cap",() -> new BlockItem(GRAY_TRASH_CAN_CAP.get(),new Item.Properties()));
        ITEMS.register("green_trash_can",() -> new BlockItem(GREEN_TRASH_CAN.get(),new Item.Properties()));
        ITEMS.register("green_trash_can_cap",() -> new BlockItem(GREEN_TRASH_CAN_CAP.get(),new Item.Properties()));

        ITEMS.register("register", () -> new BlockItem(REGISTER.get(), new Item.Properties()));
        ITEMS.register("gorgeous_register", () -> new BlockItem(GORGEOUS_REGISTER.get(), new Item.Properties()));
        ITEMS.register("mechanical_register", () -> new BlockItem(MECHANICAL_REGISTER.get(), new Item.Properties()));
        ITEMS.register("tech_register", () -> new BlockItem(TECH_REGISTER.get(), new Item.Properties()));

    }

    private static void regTrapDoors(Map<BlockSetType, Block> existing) {
        for (var type : existing.entrySet()) {
            var name = "cosmetic_" + type.getKey().name() + "_trapdoor";
            var block = BLOCKS.register(name, () -> new CosmeticTrapdoor(BlockBehaviour.Properties.ofFullCopy(type.getValue())));
            ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void regSimpleCosmetic(List<Block> existing) {
        for (var existingBlock : existing) {
            var name = "cosmetic_" + BuiltInRegistries.BLOCK.getKey(existingBlock).getPath();
            var block = BLOCKS.register(name, () -> new CosmeticSimpleBlock(BlockBehaviour.Properties.ofFullCopy(existingBlock)));
            ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }
    
    @SubscribeEvent
    public static void regBlockCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ITEM_SUPPLIER_BLOCK_ENTITY.get(),
                (be,context) -> be.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                POWER_SUPPLY_BLOCK_ENTITY.get(),
                (be,context) -> be.getEnergyStore()
        );
    }
}
