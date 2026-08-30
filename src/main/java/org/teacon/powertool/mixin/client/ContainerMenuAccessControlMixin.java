package org.teacon.powertool.mixin.client;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.teacon.powertool.api.IScreenAccessControlData;

@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMenuAccessControlMixin implements IScreenAccessControlData {
    @Unique
    boolean powerTool$isDisplayMode;

    @Override
    public void powerTool$setDisplayMode(boolean displayMode) {
        this.powerTool$isDisplayMode = displayMode;
    }

    @Override
    public boolean powerTool$getDisplayMode() {
        return this.powerTool$isDisplayMode;
    }
}
