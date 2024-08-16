package org.teacon.powertool.item;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CommandWand extends Item {
    public CommandWand(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide) {
            String command = held.get(PowerToolItems.COMMAND); // get(Supplier<DataComponentType<T>>) is from NeoForge
            if (command != null) {
                // Raise permission level to 2, akin to what vanilla sign does
                CommandSourceStack cmdSrc = player.createCommandSourceStack().withPermission(2);
                var server = level.getServer();
                if (server != null) {
                    server.getCommands().performPrefixedCommand(cmdSrc, command);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }
}
