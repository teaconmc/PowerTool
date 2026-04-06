package org.teacon.powertool.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.commons.compress.utils.Lists;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.block.cosmetical.CosmeticFurnace;

import java.util.Objects;

import static org.teacon.powertool.utils.VanillaUtils.modRL;

@NonNullByDefault
public class ModBlockModelProvider extends ModelProvider {
    
    private BlockModelGenerators blockModelGenerators;
    private ItemModelGenerators itemModelGenerators;
    
    public ModBlockModelProvider(PackOutput output) {
        super(output, DataGenerators.MOD_ID);
    }
    
    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.blockModelGenerators = blockModels;
        this.itemModelGenerators = itemModels;
        this.registerStatesAndModels();
    }
    
    protected void registerStatesAndModels() {
        var simpleBlocks = Lists.newArrayList(PowerToolBlocks.SIMPLE_BLOCKS.iterator());
        simpleBlocks.add(Blocks.ENCHANTING_TABLE);
        simpleBlocks.stream().filter(b -> b != Blocks.BEACON).forEach(block -> simpleBlockWithItem(cosmeticBlock(block)));
        cosmeticHorizontalBlockWithItem(Blocks.BEEHIVE);
        cosmeticFurnaceBlockWithItem(Blocks.FURNACE);
        cosmeticFurnaceBlockWithItem(Blocks.BLAST_FURNACE);
        cosmeticFurnaceBlockWithItem(Blocks.SMOKER);
        cosmeticDirectionalBlockWithItem(Blocks.BARREL);
        cosmeticHorizontalBlockWithItem(Blocks.STONECUTTER);
        horizontalBlockWithItem(PowerToolBlocks.WHITE_TRASH_CAN.get());
        horizontalBlockWithItem(PowerToolBlocks.GRAY_TRASH_CAN.get());
        horizontalBlockWithItem(PowerToolBlocks.GREEN_TRASH_CAN.get());
        horizontalBlockWithItem(PowerToolBlocks.TEMPLE.get());
        trashCanCap(PowerToolBlocks.WHITE_TRASH_CAN_CAP.get());
        //trashCanCap(PowerToolBlocks.GRAY_TRASH_CAN_CAP.get());
        trashCanCap(PowerToolBlocks.GREEN_TRASH_CAN_CAP.get());
        cosmeticBlock(Blocks.BEACON);
        cosmeticBlock(Blocks.ENCHANTING_TABLE);
        simpleBlockWithItem(cosmeticBlock(Blocks.BEACON), models().withExistingParent(name(cosmeticBlock(Blocks.BEACON)), mcLoc(name(Blocks.BEACON))).renderType("cutout"));
        modItem(PowerToolBlocks.DELAYER.get());
        for (var entry : PowerToolBlocks.DH_CHEAT_BLOCKS.entrySet()) {
            var dye = entry.getKey();
            var block = entry.getValue().get();
            simpleBlock(block, models().withExistingParent(name(block), "block/block").texture("particle", mcLoc("block/" + dye.getName() + "_concrete")));
            itemModels().getBuilder(name(block)).parent(models().getExistingFile(mcLoc("block/" + dye.getName() + "_concrete")));
        }
    }
    
    private void simpleBlockWithItem(Block block) {
        
        this.blockModelGenerators.registerSimpleItemModel(block, modRL(name(block)));
    }
    
    private void trashCanCap(TrapDoorBlock block) {
        var name = name(block);
        trapdoorBlock(block, models().getExistingFile(modLoc(name + "_bottom")),
                models().getExistingFile(modLoc(name + "_top")),
                models().getExistingFile(modLoc(name + "_open")), true);
        simpleBlockItem(block, models().getExistingFile(modLoc(name + "_bottom")));
    }
    
    private Block cosmeticBlock(Block block) {
        return BuiltInRegistries.BLOCK.get(modLoc("cosmetic_" + name(block)));
    }
    
    private static String name(Block block) {
        return Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getPath();
    }
    
    private void horizontalBlockWithItem(Block block) {
        var model = models().getExistingFile(modLoc(name(block)));
        horizontalBlock(block, model);
        simpleBlockItem(block, model);
    }
    
    @SuppressWarnings("SameParameterValue")
    private void cosmeticHorizontalBlockWithItem(Block block) {
        this.blockModelGenerators.createHorizontallyRotatedBlock();
        horizontalBlock(cosmeticBlock(block), models().getExistingFile(mcLoc(name(block))));
        item(block);
    }
    
    @SuppressWarnings("SameParameterValue")
    private void cosmeticDirectionalBlockWithItem(Block block) {
        directionalBlock(cosmeticBlock(block), models().getExistingFile(mcLoc(name(block))));
        item(block);
    }
    
    private void cosmeticFurnaceBlockWithItem(Block block) {
        getVariantBuilder(cosmeticBlock(block)).forAllStates(state -> {
            var model = state.getValue(CosmeticFurnace.LIT) ? models().getExistingFile(mcLoc(name(block) + "_on")) : models().getExistingFile(mcLoc(name(block)));
            return ConfiguredModel.builder().modelFile(model).rotationY((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180).build();
        });
        item(block);
    }
    
    private void item(Block block) {
        itemModels().getBuilder(name(cosmeticBlock(block))).parent(models().getExistingFile(mcLoc(name(block))));
    }
    
    private void modItem(Block block) {
        itemModels().getBuilder(name(block)).parent(models().getExistingFile(modLoc(name(block))));
    }
    
}
