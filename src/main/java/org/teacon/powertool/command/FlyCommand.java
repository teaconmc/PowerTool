package org.teacon.powertool.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.teacon.powertool.PowerTool;

public class FlyCommand {

    public static final ResourceLocation POWER_TOOL_FLY_MODIFIER = ResourceLocation.fromNamespaceAndPath(PowerTool.MODID, "creative_flight");

    public static void reg(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fly").requires(p -> p.hasPermission(2)).executes(FlyCommand::fly0));
    }

    private static int fly0(CommandContext<CommandSourceStack> sourceStack) {
        var p = sourceStack.getSource().getPlayer();
        if (p != null) {
            // https://github.com/neoforged/NeoForge/pull/724
            // The gist:
            // - Positive value means you can fly
            // - Zero or negative value means you cannot fly
            // This way, creative flight from multiple mods can stack together without clash.
            // Attributes are also automatically synced to client, so less hassle on our side.
            var flyAttribute = p.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
            var gameMode = p.gameMode;
            if (gameMode.isSurvival()) {
                if (flyAttribute != null) {
                    if (flyAttribute.hasModifier(POWER_TOOL_FLY_MODIFIER)) {
                        flyAttribute.removeModifier(POWER_TOOL_FLY_MODIFIER);
                        sourceStack.getSource().sendSuccess(() -> Component.translatable("powertool.command.fly.disabled"), true);
                    } else {
                        flyAttribute.addPermanentModifier(new AttributeModifier(POWER_TOOL_FLY_MODIFIER, 943, AttributeModifier.Operation.ADD_VALUE));
                        sourceStack.getSource().sendSuccess(() -> Component.translatable("powertool.command.fly.enabled"), true);
                    }
                } else {
                    sourceStack.getSource().sendSuccess(() -> Component.translatable("powertool.command.fly.unavailable"), true);
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
