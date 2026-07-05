package org.teacon.powertool.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.teacon.powertool.FlyNoDrift;

import java.util.Collection;

public class FlyNoDriftCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("powertool").then(createNode()));
        dispatcher.register(Commands.literal("pt").then(createNode()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createNode() {
        return Commands.literal("flyNoDrift")
                .requires(p -> p.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(FlyNoDriftCommand::query)
                .then(Commands.literal("enable").executes(context -> set(context, true)))
                .then(Commands.literal("disable").executes(context -> set(context, false)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> set(context, BoolArgumentType.getBool(context, "enabled"))))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> setTargets(
                                        context,
                                        EntityArgument.getPlayers(context, "targets"),
                                        BoolArgumentType.getBool(context, "enabled")
                                ))));
    }

    private static int query(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        context.getSource().sendSuccess(() -> Component.translatable(
                FlyNoDrift.enabled(player)
                        ? "powertool.command.fly_no_drift.enabled"
                        : "powertool.command.fly_no_drift.disabled"
        ), false);
        return FlyNoDrift.enabled(player) ? 1 : 0;
    }

    private static int set(CommandContext<CommandSourceStack> context, boolean enabled) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        FlyNoDrift.setEnabled(player, enabled);
        context.getSource().sendSuccess(() -> Component.translatable(
                enabled
                        ? "powertool.command.fly_no_drift.enabled"
                        : "powertool.command.fly_no_drift.disabled"
        ), true);
        return enabled ? 1 : 0;
    }

    private static int setTargets(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, boolean enabled) {
        for (ServerPlayer player : targets) {
            FlyNoDrift.setEnabled(player, enabled);
        }
        context.getSource().sendSuccess(() -> Component.translatable(
                enabled
                        ? "powertool.command.fly_no_drift.enabled.targets"
                        : "powertool.command.fly_no_drift.disabled.targets",
                targets.size()
        ), true);
        return targets.size();
    }
}
