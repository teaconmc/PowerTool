package org.teacon.powertool.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public interface IScreenProviderItem {
    
    @OnlyIn(Dist.CLIENT)
    Supplier<Screen> getScreenSupplier(ItemStack stack, EquipmentSlot slot);
}
