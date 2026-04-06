package org.teacon.powertool.client.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.teacon.powertool.entity.MartingCarEntity;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MartingCarOverlay {
    
    public static void renderBoostBar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        var player = Minecraft.getInstance().player;
        if (player == null || !player.isPassenger()) return;
        var vehicle = player.getVehicle();
        if (!(vehicle instanceof MartingCarEntity martingCar)) return;
        var font = Minecraft.getInstance().font;
        float energy = martingCar.getEntityData().get(MartingCarEntity.ENERGY);
        //VanillaUtils.recordDebugData("energy",(long) energy);
        float ratio = Mth.clamp(energy / (float) MartingCarEntity.MAX_ENERGY, 0f, 1f);
        guiGraphics.renderOutline(guiGraphics.guiWidth() / 2 - 100, guiGraphics.guiHeight() - 50, 200, 10, -1);
        guiGraphics.fill(guiGraphics.guiWidth() / 2 - 99, guiGraphics.guiHeight() - 49, guiGraphics.guiWidth() / 2 - 99 + (int) (198 * ratio), guiGraphics.guiHeight() - 49 + 8, ratio == 1 ? VanillaUtils.getColor(0, 255, 0, 255) : -1);
        guiGraphics.drawString(font, "boost", guiGraphics.guiWidth() / 2 - 102 - font.width("boost"), guiGraphics.guiHeight() - 48, -1, true);
    }
}
