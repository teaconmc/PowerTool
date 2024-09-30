package org.teacon.powertool.utils;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import org.teacon.powertool.PowerTool;

public class VanillaUtils {
    
    public static ResourceLocation modRL(String path) {
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
    
    public static void runCommand(String command, LivingEntity livingEntity) {
        // Raise permission level to 2, akin to what vanilla sign does
        CommandSourceStack cmdSrc = livingEntity.createCommandSourceStack().withPermission(2);
        var server = livingEntity.level().getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(cmdSrc, command);
        }
    }
    
    //irrelevant vanilla(笑)
    public static int getColor(int r,int g,int b,int a){
        return a << 24 | r << 16 | g << 8 | b;
    }
    
    @SuppressWarnings("SuspiciousNameCombination")
    public static Vec2 rotate90FromBlockCenterYP(Vec2 point, int times) {
        times = times % 4;
        if (times == 0) return point;
        var x = point.x;
        var y = point.y;
        if (times == 1) return new Vec2(16-y,x);
        if (times == 2) return new Vec2(16-x,16-y);
        return new Vec2(y,16-x);
    }
}
