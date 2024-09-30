package org.teacon.powertool.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;
import org.teacon.powertool.PowerTool;

import java.util.concurrent.CompletableFuture;

public class SpriteProvider extends SpriteSourceProvider {
    
    public SpriteProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, PowerTool.MODID, existingFileHelper);
    }
    
    @Override
    protected void gather() {
        var source = atlas(BLOCKS_ATLAS);
        //source.addSource(new DirectoryLister("gui/sprite","gui/sprite/"));
    }
}
