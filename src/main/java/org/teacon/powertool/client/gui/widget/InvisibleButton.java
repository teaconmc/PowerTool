package org.teacon.powertool.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 * Special button that only renders tooltip and triggers narration.
 */
public class InvisibleButton extends Button {
    public InvisibleButton(Builder builder) {
        super(builder);
    }
    
    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
    }
    
    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    
    }
}
