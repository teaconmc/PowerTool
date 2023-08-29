package org.teacon.powertool.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.teacon.powertool.block.CosmeticFurnace;
import org.teacon.powertool.block.PowerToolBlocks;

import java.util.Objects;

public class ModBlockModelProvider extends BlockStateProvider {

    public ModBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, DataGenerators.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        PowerToolBlocks.SIMPLE_BLOCKS.forEach(block -> simpleBlockWithItem(cosmeticBlock(block), models().getExistingFile(mcLoc(name(block)))));
        cosmeticHorizontalBlockWithItem(Blocks.BEEHIVE);
        cosmeticFurnaceBlockWithItem(Blocks.FURNACE);
        cosmeticFurnaceBlockWithItem(Blocks.BLAST_FURNACE);
        cosmeticDirectionalBlockWithItem(Blocks.BARREL);
    }

    private Block cosmeticBlock(Block block){
        return ForgeRegistries.BLOCKS.getValue(modLoc("cosmetic_" + name(block)));
    }

    private static String name(Block block){
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath();
    }

    private void cosmeticHorizontalBlockWithItem(Block block){
        horizontalBlock(cosmeticBlock(block), models().getExistingFile(mcLoc(name(block))));
        item(block);
    }

    private void cosmeticDirectionalBlockWithItem(Block block){
        directionalBlock(cosmeticBlock(block), models().getExistingFile(mcLoc(name(block))));
        item(block);
    }

    private void cosmeticFurnaceBlockWithItem(Block block){
        getVariantBuilder(cosmeticBlock(block)).forAllStates(state -> {
            var model = state.getValue(CosmeticFurnace.LIT) ? models().getExistingFile(mcLoc(name(block) + "_on")) : models().getExistingFile(mcLoc(name(block)));
            return ConfiguredModel.builder().modelFile(model).rotationY((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180).build();
        });
        item(block);
    }

    private void item(Block block) {
        itemModels().getBuilder(name(cosmeticBlock(block))).parent(models().getExistingFile(mcLoc(name(block))));
    }


}
