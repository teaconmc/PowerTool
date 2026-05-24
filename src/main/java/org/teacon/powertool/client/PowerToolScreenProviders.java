package org.teacon.powertool.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.teacon.powertool.client.gui.ExamineHoloGlassScreen;
import org.teacon.powertool.client.gui.SetCommandScreen;
import org.teacon.powertool.item.PowerToolDataComponents;
import org.teacon.powertool.item.PowerToolItems;

import java.util.HashMap;
import java.util.Map;

public class PowerToolScreenProviders {
    
    public static final Map<Identifier, IScreenProviderItemClient> SCREEN_PROVIDERS = new HashMap<>();
    
    public static void init(){
        SCREEN_PROVIDERS.put(PowerToolItems.COMMAND_RUNE.getId(), SetCommandScreen::new);
        SCREEN_PROVIDERS.put(PowerToolItems.EXAMINE_HOLO_GLASS.getId(), (stack, slot) -> new ExamineHoloGlassScreen(slot, stack.get(PowerToolDataComponents.BLOCK_TAGS_DATA), stack.get(PowerToolDataComponents.BLOCKS_DATA)));
    }
    
    public interface IScreenProviderItemClient{
        Screen createScreen(ItemStack stack, EquipmentSlot slot);
    }
}
