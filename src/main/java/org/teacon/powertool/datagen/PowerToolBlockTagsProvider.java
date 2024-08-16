package org.teacon.powertool.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.block.PowerToolBlocks;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class PowerToolBlockTagsProvider extends BlockTagsProvider {
    
    public static final TagKey<Block> COMMAND_BLOCK_TAG = BlockTags.create(VanillaUtils.modResourceLocation("command_block"));
    public static final TagKey<Block> REPEATING_COMMAND_BLOCK_TAG = BlockTags.create(VanillaUtils.modResourceLocation("repeating_command_block"));
    
    public PowerToolBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, PowerTool.MODID, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.WITHER_IMMUNE).add(PowerToolBlocks.COMMAND_BLOCK.get());
        this.tag(BlockTags.DRAGON_IMMUNE).add(PowerToolBlocks.COMMAND_BLOCK.get());
        this.tag(COMMAND_BLOCK_TAG)
                .add(
                        PowerToolBlocks.COMMAND_BLOCK.get(),
                        Blocks.COMMAND_BLOCK,
                        Blocks.CHAIN_COMMAND_BLOCK,
                        Blocks.REPEATING_COMMAND_BLOCK
                        );
        this.tag(REPEATING_COMMAND_BLOCK_TAG)
                .add(
                        PowerToolBlocks.COMMAND_BLOCK.get(),
                        Blocks.REPEATING_COMMAND_BLOCK);
    }
}
