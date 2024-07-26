package org.teacon.powertool.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import org.teacon.powertool.PowerTool;

public class VanillaUtils {
    
    public static ResourceLocation modResourceLocation(String path) {
        return resourceLocationOf(PowerTool.MODID, path);
    }
    
    public static ResourceLocation resourceLocationOf(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
    
    public static EquipmentSlot equipmentSlotFromHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }
    
    public static ItemInteractionResult itemInteractionFrom(InteractionResult result) {
        return switch (result){
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            case FAIL -> ItemInteractionResult.FAIL;
        };
    }
    
    //irrelevant vanilla(笑)
    public static int getColor(int r,int g,int b,int a){
        return a << 24 | r << 16 | g << 8 | b;
    }
}
