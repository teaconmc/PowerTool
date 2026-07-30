package org.teacon.powertool.compat.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.BlockItem;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.client.gui.TextureExtractorScreen;

import java.util.List;

@NonNullByDefault
public class TextureExtractorGhostIngredientHandler implements IGhostIngredientHandler<TextureExtractorScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(TextureExtractorScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        var is = ingredient.getItemStack();
        if(is.isPresent()){
            var stack = is.get();
            if(!(stack.getItem() instanceof BlockItem)) return List.of();
            return List.of(new Target<>() {
                @Override
                public Rect2i getArea() {
                    return new Rect2i(screen.filterSlotX(), screen.filterSlotY(), screen.filterSlotSize(), screen.filterSlotSize());
                }
                
                @Override
                public void accept(I ignored) {
                    screen.setFilterStack(stack);
                }
            });
        }
        return List.of();
    }

    @Override
    public void onComplete() {
    }
}
