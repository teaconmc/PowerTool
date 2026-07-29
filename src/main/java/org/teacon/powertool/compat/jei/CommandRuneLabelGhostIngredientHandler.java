package org.teacon.powertool.compat.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.client.gui.CommandRuneLabelSelectScreen;

import java.util.List;

@NonNullByDefault
public class CommandRuneLabelGhostIngredientHandler implements IGhostIngredientHandler<CommandRuneLabelSelectScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(CommandRuneLabelSelectScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        var is = ingredient.getItemStack();
        if (is.isPresent()){
            var stack = is.get();
            return List.of(new Target<>() {
                @Override
                public Rect2i getArea() {
                    return new Rect2i(screen.slotX(), screen.slotY(), screen.slotSize(), screen.slotSize());
                }
                
                @Override
                public void accept(I ignored) {
                    screen.acceptLabel(stack);
                }
            });
        }
        return List.of();
    }

    @Override
    public void onComplete() {
    }
}
