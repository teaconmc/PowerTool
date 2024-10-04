package org.teacon.powertool.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.client.gui.SetCommandScreen;
import org.teacon.powertool.network.client.OpenItemScreen;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandRune extends Item implements IScreenProviderItem{
    public CommandRune(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return stack.getOrDefault(PowerToolItems.CYCLE, 0); // getOrDefault(Supplier<DataComponentType<T>>, T) is from NeoForge
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        String command = held.get(PowerToolItems.COMMAND);
        if (command == null) {
             if(player instanceof ServerPlayer serverPlayer){
                PacketDistributor.sendToPlayer(serverPlayer,
                        new OpenItemScreen(player.getItemInHand(hand),hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
            }
            return InteractionResultHolder.pass(held);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(held);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide) {
            String command = stack.get(PowerToolItems.COMMAND); // get(Supplier<DataComponentType<T>>) is from NeoForge
            if (command != null) {
                VanillaUtils.runCommand(command, livingEntity);
                // Yes, you can make it consumable
                // Viva la data component!
                EquipmentSlot slot = stack.equals(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND))
                        ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                stack.hurtAndBreak(1, livingEntity, slot);
            }
            
        }
        return stack;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Supplier<Screen> getScreenSupplier(ItemStack stack, EquipmentSlot slot) {
        return () -> new SetCommandScreen(stack,slot);
    }
}
