package org.teacon.powertool.client.gui.inspector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class InspectorTitle extends InspectorWidget {
    private final Component message;

    public InspectorTitle(Component message, int height) {
        super(Math.max(height, 16));
        this.message = message;
    }

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, int width, int mouseX, int mouseY, float partialTick, boolean hovered) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(1.1f, 1.1f);
        graphics.text(Minecraft.getInstance().font, this.message, 0, this.getHeight() / 2, 0xffffffff);
        pose.popMatrix();
    }
}
