package org.teacon.powertool.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.item.PowerToolItems;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class PowerToolItemTagsProvider extends ItemTagsProvider {
    
    public static final TagKey<Item> TONK = ItemTags.create(VanillaUtils.modRL("tonk"));
    
    public PowerToolItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, PowerTool.MODID);
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
