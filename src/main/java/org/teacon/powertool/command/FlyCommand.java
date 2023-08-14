package org.teacon.powertool.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;

public class FlyCommand {
    public static void reg(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fly").executes(FlyCommand::fly0));
    }

    private static int fly0(CommandContext<CommandSourceStack> sourceStack) {
        var p = sourceStack.getSource().getPlayer();
        if (p != null) {
            var abilities = p.getAbilities();
            var gameMode = p.gameMode;
            if (gameMode.isSurvival()) {
                abilities.mayfly = !abilities.mayfly;
                p.connection.send(new ClientboundPlayerAbilitiesPacket(p.getAbilities()));
                if (abilities.mayfly) {
                    sourceStack.getSource().sendSuccess(() -> Component.translatable("powertool.command.fly.enabled"), true);
                } else {
                    sourceStack.getSource().sendSuccess(() -> Component.translatable("powertool.command.fly.disabled"), true);
                }
            } else {
                sourceStack.getSource().sendSuccess(() -> Component.translatable("powertool.command.fly.not_changed"), true);
            }
            return Command.SINGLE_SUCCESS;
        } else {
            sourceStack.getSource().sendFailure(Component.translatable("powertool.command.fly.must_be_player"));
            return 0;
        }
    }
}
