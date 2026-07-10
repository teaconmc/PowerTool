package org.teacon.powertool.exhibition.node;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.teacon.powertool.entity.exhibit.ExhibitionEntity;
import org.teacon.powertool.inspection.Duplicatable;
import org.teacon.powertool.inspection.Inspectable;
import org.teacon.powertool.inspection.InspectorBuilder;
import org.teacon.powertool.inspection.constraint.NumberConstraint;
import org.teacon.powertool.inspection.property.BooleanProperty;
import org.teacon.powertool.inspection.property.IntegerProperty;
import org.teacon.powertool.inspection.property.StringProperty;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandNode extends ExhibitionNode implements Inspectable {

    public static final MapCodec<CommandNode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(CommandNode::getName),
            Codec.STRING.fieldOf("command").forGetter(CommandNode::getCommand),
            Codec.BOOL.optionalFieldOf("asPlayer", false).forGetter(CommandNode::asPlayer),
            Codec.INT.optionalFieldOf("permission", 0).forGetter(CommandNode::getPermission)
    ).apply(instance, CommandNode::new));

    public static final StreamCodec<ByteBuf, CommandNode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CommandNode::getName,
            ByteBufCodecs.STRING_UTF8,
            CommandNode::getCommand,
            ByteBufCodecs.BOOL,
            CommandNode::asPlayer,
            ByteBufCodecs.VAR_INT,
            CommandNode::getPermission,
            CommandNode::new
    );

    private static final NumberConstraint<Integer> PERMISSION_CONSTRAINT = NumberConstraint.integer(
            0,
            4,
            0
    );

    private final String name;

    private final StringProperty    command;
    private final BooleanProperty   asPlayer;
    private final IntegerProperty   permission;

    public CommandNode(
            final String name
    ) {
        this(
                name,
                "",
                false,
                0
        );
    }

    private CommandNode(
            final String name,
            final String command,
            final boolean asPlayer,
            final int permission
    ) {
        this.name       = name;
        this.command    = StringProperty.simple(command);
        this.asPlayer   = BooleanProperty.simple(asPlayer);
        this.permission = IntegerProperty.simple(permission);
    }

    @Override
    public String type() {
        return "command";
    }

    @Override
    public CommandNode duplicate() {
        return new CommandNode(
                this.name,
                this.command.get(),
                this.asPlayer.get(),
                this.permission.get()
        );
    }

    @Override
    public void paste(final Duplicatable copy) {
        if (copy.getClass() != CommandNode.class) {
            return;
        }

        final var node = (CommandNode) copy;
        this.command.set(node.command.get());
        this.asPlayer.set(node.asPlayer.get());
        this.permission.set(node.permission.get());
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void onInspect(final InspectorBuilder builder) {
        builder .title(Component.literal(this.name))
                .inputString(Component.literal("Command"), this.command)
                .sliderInt(Component.literal("Permission Level"), this.permission, PERMISSION_CONSTRAINT, 1)
                .checkbox(Component.literal("As Player"), this.asPlayer);
    }

    public String getName() {
        return this.name;
    }

    public String getCommand() {
        return this.command.get();
    }

    public boolean asPlayer() {
        return this.asPlayer.get();
    }

    public int getPermission() {
        return this.permission.get();
    }

    public boolean invoke(
            final ExhibitionEntity  entity,
            final Player            player
    ) {

        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }

        final var command = this.command.get();
        if (command.isEmpty()) {
            return false;
        }

        final var executor = this.asPlayer.get() ? player : entity;

        CommandSourceStack cmdSrc = executor
                .createCommandSourceStackForNameResolution(level)
                .withPermission(LevelBasedPermissionSet.forLevel(PermissionLevel.byId(this.permission.getValue())));
        var server = level.getServer();
        server.getCommands().performPrefixedCommand(cmdSrc, command);

        return true;
    }
}
