package org.teacon.powertool.item;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class CommandWand extends Item {
    public CommandWand(Properties properties) {
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack held = player.getItemInHand(usedHand);
        String command = held.get(PowerToolItems.COMMAND);
        if (command == null) {
            return InteractionResultHolder.pass(held);
        }
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(held);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide) {
            String command = stack.get(PowerToolItems.COMMAND); // get(Supplier<DataComponentType<T>>) is from NeoForge
            if (command != null) {
                // Raise permission level to 2, akin to what vanilla sign does
                CommandSourceStack cmdSrc = livingEntity.createCommandSourceStack().withPermission(2);
                var server = level.getServer();
                if (server != null) {
                    server.getCommands().performPrefixedCommand(cmdSrc, command);
                }
            }
        }
        return stack;
    }
}
