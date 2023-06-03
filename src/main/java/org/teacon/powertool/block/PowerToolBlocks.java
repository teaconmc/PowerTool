package org.teacon.powertool.block;

import com.mojang.datafixers.DSL;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.entity.HolographicSignBlockEntity;
import org.teacon.powertool.block.entity.ItemDisplayBlockEntity;
import org.teacon.powertool.block.entity.ItemSupplierBlockEntity;
import org.teacon.powertool.block.entity.PeriodicCommandBlockEntity;
import org.teacon.powertool.block.entity.PowerSupplyBlockEntity;

import static org.teacon.powertool.item.PowerToolItems.ITEMS;

public class PowerToolBlocks {

    public static final SoundType ITEM_DISPLAY_SOUND_TYPE = new ForgeSoundType(1.0F, 1.0F,
            () -> SoundEvents.ITEM_FRAME_BREAK,
            () -> SoundEvents.MOSS_CARPET_STEP,
            () -> SoundEvents.ITEM_FRAME_PLACE,
            () -> SoundEvents.ITEM_FRAME_REMOVE_ITEM,
            () -> SoundEvents.MOSS_CARPET_FALL
            );

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PowerTool.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PowerTool.MODID);

    public static RegistryObject<Block> COMMAND_BLOCK;
    public static RegistryObject<Block> TRASH_CAN;
    public static RegistryObject<Block> POWER_SUPPLY;
    public static RegistryObject<Block> ITEM_DISPLAY;
    public static RegistryObject<Block> ITEM_SUPPLIER;

    public static RegistryObject<Block> SLIM_ITEM_SUPPLIER;
    public static RegistryObject<Block> COSMETIC_HOPPER;
    public static RegistryObject<Block> COSMETIC_CAMPFIRE;
    public static RegistryObject<Block> COSMETIC_SOUL_CAMPFIRE;
    public static RegistryObject<Block> HOLOGRAPHIC_SIGN;
    public static RegistryObject<BlockEntityType<PeriodicCommandBlockEntity>> COMMAND_BLOCK_ENTITY;
    public static RegistryObject<BlockEntityType<PowerSupplyBlockEntity>> POWER_SUPPLY_BLOCK_ENTITY;

    public static RegistryObject<BlockEntityType<ItemDisplayBlockEntity>> ITEM_DISPLAY_BLOCK_ENTITY;
    public static RegistryObject<BlockEntityType<ItemSupplierBlockEntity>> ITEM_SUPPLIER_BLOCK_ENTITY;
    public static RegistryObject<BlockEntityType<HolographicSignBlockEntity>> HOLOGRAPHIC_SIGN_BLOCK_ENTITY;

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        COMMAND_BLOCK = BLOCKS.register("command_block", () -> new PeriodicCommandBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable(),
            false
        ));
        TRASH_CAN = BLOCKS.register("trash_can", () -> new TrashCanBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1000)));
        POWER_SUPPLY = BLOCKS.register("power_supply", () -> new PowerSupplyBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1000)));
        ITEM_DISPLAY = BLOCKS.register("item_display", () -> new ItemDisplayBlock(BlockBehaviour.Properties.copy(Blocks.LADDER).sound(ITEM_DISPLAY_SOUND_TYPE).noOcclusion().strength(10000)));
        ITEM_SUPPLIER = BLOCKS.register("item_supplier", () -> new ItemSupplierBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1000).noOcclusion()));
        SLIM_ITEM_SUPPLIER = BLOCKS.register("slim_item_supplier", () -> new SlimItemSupplierBlock(BlockBehaviour.Properties.of(Material.METAL).strength(1000).noOcclusion()));
        COSMETIC_HOPPER = BLOCKS.register("cosmetic_hopper", () -> new CosmeticHopper(BlockBehaviour.Properties.copy(Blocks.HOPPER)));
        COSMETIC_CAMPFIRE = BLOCKS.register("cosmetic_campfire", () -> new CosmeticCampfireBlock(true, BlockBehaviour.Properties.copy(Blocks.CAMPFIRE)));
        COSMETIC_SOUL_CAMPFIRE = BLOCKS.register("cosmetic_soul_campfire", () -> new CosmeticCampfireBlock(false, BlockBehaviour.Properties.copy(Blocks.SOUL_CAMPFIRE)));
        HOLOGRAPHIC_SIGN = BLOCKS.register("holographic_sign", () -> new HolographicSignBlock(BlockBehaviour.Properties.copy(Blocks.LIGHT).lightLevel(state -> 15).noCollission().noLootTable()));
        COMMAND_BLOCK_ENTITY = BLOCK_ENTITIES.register("command_block_entity", () -> BlockEntityType.Builder.of(
            PeriodicCommandBlockEntity::new, COMMAND_BLOCK.get()
        ).build(DSL.remainderType()));
        POWER_SUPPLY_BLOCK_ENTITY = BLOCK_ENTITIES.register("power_supply", () -> BlockEntityType.Builder.of(
                PowerSupplyBlockEntity::new, POWER_SUPPLY.get()
        ).build(DSL.remainderType()));
        ITEM_DISPLAY_BLOCK_ENTITY = BLOCK_ENTITIES.register("item_display", () -> BlockEntityType.Builder.of(
                ItemDisplayBlockEntity::new, ITEM_DISPLAY.get()
        ).build(DSL.remainderType()));
        ITEM_SUPPLIER_BLOCK_ENTITY = BLOCK_ENTITIES.register("item_supplier", () -> BlockEntityType.Builder.of(
                ItemSupplierBlockEntity::new, ITEM_SUPPLIER.get(), SLIM_ITEM_SUPPLIER.get()
        ).build(DSL.remainderType()));
        HOLOGRAPHIC_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("holographic_sign", () -> BlockEntityType.Builder.of(
                HolographicSignBlockEntity::new, HOLOGRAPHIC_SIGN.get()
        ).build(DSL.remainderType()));

        ITEMS.register("command_block", () -> new BlockItem(COMMAND_BLOCK.get(), new Item.Properties()));
        ITEMS.register("trash_can", () -> new BlockItem(TRASH_CAN.get(), new Item.Properties()));
        ITEMS.register("power_supply", () -> new BlockItem(POWER_SUPPLY.get(), new Item.Properties()));
        ITEMS.register("item_display", () -> new BlockItem(ITEM_DISPLAY.get(), new Item.Properties()));
        ITEMS.register("slim_item_supplier", () -> new BlockItem(SLIM_ITEM_SUPPLIER.get(), new Item.Properties()));
        ITEMS.register("item_supplier", () -> new BlockItem(ITEM_SUPPLIER.get(), new Item.Properties()));
        ITEMS.register("cosmetic_hopper", () -> new BlockItem(COSMETIC_HOPPER.get(), new Item.Properties()));
        ITEMS.register("cosmetic_campfire", () -> new BlockItem(COSMETIC_CAMPFIRE.get(), new Item.Properties()));
        ITEMS.register("cosmetic_soul_campfire", () -> new BlockItem(COSMETIC_SOUL_CAMPFIRE.get(), new Item.Properties()));
        ITEMS.register("holographic_sign", () -> new BlockItem(HOLOGRAPHIC_SIGN.get(), new Item.Properties()));
    }
}
