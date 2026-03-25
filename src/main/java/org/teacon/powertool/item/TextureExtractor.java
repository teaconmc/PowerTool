package org.teacon.powertool.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.teacon.powertool.menu.TextureExtractorMenu;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TextureExtractor extends Item {
    
    public TextureExtractor(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        if(level.isClientSide) return InteractionResult.SUCCESS;
        player.openMenu(new TextureExtractorMenu.Provider());
        return InteractionResult.PASS;
    }
}
