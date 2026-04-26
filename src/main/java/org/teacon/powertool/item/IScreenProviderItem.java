package org.teacon.powertool.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public interface IScreenProviderItem {
    
    Supplier<Screen> getScreenSupplier(ItemStack stack, EquipmentSlot slot);
}
