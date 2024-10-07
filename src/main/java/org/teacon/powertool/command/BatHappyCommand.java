package org.teacon.powertool.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.network.client.UpdatePlayerMovement;

/**
 * Give the player an acceleration.
 */
public class BatHappyCommand {

    public static final LiteralArgumentBuilder<CommandSourceStack> COMMAND = Commands.literal("bathappy")
            .requires(p -> p.hasPermission(2))
            .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("multiply")
                            .then(Commands.argument("factor", DoubleArgumentType.doubleArg())
                                    .executes(BatHappyCommand::multiplyMotion)
                            )
                            .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                    .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                    .executes(BatHappyCommand::multiplyMotionXyz)
                                            )
                                    )
                            )
                    )
                    .then(Commands.literal("set")
                            .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                    .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                    .executes(BatHappyCommand::setMotion)
                                            )
                                    )
                            )
                    )
                    .then(Commands.literal("add")
                            .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                    .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                    .executes(BatHappyCommand::addMotion)
                                            )
                                    )
                            )
                    )
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(COMMAND);
    }

    private static int multiplyMotion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");
        var factor = DoubleArgumentType.getDouble(context, "factor");
        PacketDistributor.sendToPlayer(player, new UpdatePlayerMovement(UpdatePlayerMovement.Operation.MULTIPLY, factor, factor, factor));
        return 1;
    }

    private static int multiplyMotionXyz(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");
        var factorX = DoubleArgumentType.getDouble(context, "x");
        var factorY = DoubleArgumentType.getDouble(context, "y");
        var factorZ = DoubleArgumentType.getDouble(context, "z");
        PacketDistributor.sendToPlayer(player, new UpdatePlayerMovement(UpdatePlayerMovement.Operation.MULTIPLY, factorX, factorY, factorZ));
        return 1;
    }

    private static int addMotion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");
        var x = DoubleArgumentType.getDouble(context, "x");
        var y = DoubleArgumentType.getDouble(context, "y");
        var z = DoubleArgumentType.getDouble(context, "z");
        PacketDistributor.sendToPlayer(player, new UpdatePlayerMovement(UpdatePlayerMovement.Operation.ADD, x, y, z));
        return 1;
    }

    private static int setMotion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(context, "player");
        var x = DoubleArgumentType.getDouble(context, "x");
        var y = DoubleArgumentType.getDouble(context, "y");
        var z = DoubleArgumentType.getDouble(context, "z");
        PacketDistributor.sendToPlayer(player, new UpdatePlayerMovement(UpdatePlayerMovement.Operation.SET, x, y, z));
        return 1;
    }
}
