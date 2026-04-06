package org.teacon.powertool.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.menu.TextureExtractorMenu;

@NonNullByDefault
public class TextureExtractor extends Item {
    
    public TextureExtractor(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        player.openMenu(new TextureExtractorMenu.Provider());
        return InteractionResult.PASS;
    }
}
