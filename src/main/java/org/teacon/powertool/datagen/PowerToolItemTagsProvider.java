package org.teacon.powertool.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.item.PowerToolItems;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class PowerToolItemTagsProvider extends ItemTagsProvider {
    
    public static final TagKey<Item> TONK = ItemTags.create(VanillaUtils.modRL("tonk"));
    
    public PowerToolItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, PowerTool.MODID, existingFileHelper);
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(TONK).add(
                PowerToolItems.TONK.get(),
                PowerToolItems.THICK_TONK.get(),
                PowerToolItems.EXTRA_THICK_TONK.get()
        );
    }
}
