package org.teacon.powertool;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.teacon.powertool.annotation.NonNullByDefault;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NonNullByDefault
public final class CreativeBlockBreakUndo {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_HISTORY_SIZE = 16;
    private static final Map<UUID, Deque<Snapshot>> HISTORIES = new HashMap<>();

    private CreativeBlockBreakUndo() {
    }

    public static @Nullable Snapshot capture(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level();
        BlockState state = level.getBlockState(pos);
        if (!player.isCreative() || state.isAir()) {
            return null;
        }

        ItemStack itemStack = state.getCloneItemStack(pos, level, true, player);
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        if (!itemStack.isEmpty() && blockEntity != null) {
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
                blockEntity.saveCustomOnly(output);
                blockEntity.removeComponentsFromTag(output);
                BlockItem.setBlockEntityData(itemStack, blockEntity.getType(), output);
                itemStack.applyComponents(blockEntity.collectComponents());
            }
        }
        return new Snapshot(level.dimension(), pos.immutable(), state, itemStack);
    }

    public static void record(ServerPlayer player, Snapshot snapshot) {
        Deque<Snapshot> history = HISTORIES.computeIfAbsent(player.getUUID(), _ -> new ArrayDeque<>());
        history.addFirst(snapshot);
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }
    }

    public static void undo(ServerPlayer player) {
        Deque<Snapshot> history = HISTORIES.get(player.getUUID());
        if (history == null || history.isEmpty()) {
            player.sendSystemMessage(Component.translatable("powertool.creative_block_break_undo.empty"));
            return;
        }

        Snapshot snapshot = history.peekFirst();
        ServerLevel level = player.level().getServer().getLevel(snapshot.dimension());
        if (level == null || !level.getBlockState(snapshot.pos()).isAir()) {
            player.sendSystemMessage(message("powertool.creative_block_break_undo.failed", snapshot));
            return;
        }

        if (!level.setBlock(snapshot.pos(), snapshot.state(), Block.UPDATE_ALL)) {
            player.sendSystemMessage(message("powertool.creative_block_break_undo.failed", snapshot));
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(snapshot.pos());
        if (blockEntity != null && !snapshot.itemStack().isEmpty()) {
            BlockItem.updateCustomBlockEntityTag(level, player, snapshot.pos(), snapshot.itemStack());
            blockEntity.applyComponentsFromItemStack(snapshot.itemStack());
            blockEntity.setChanged();
            level.sendBlockUpdated(snapshot.pos(), snapshot.state(), snapshot.state(), Block.UPDATE_CLIENTS);
        }

        history.removeFirst();
        if (history.isEmpty()) {
            HISTORIES.remove(player.getUUID());
        }
        player.sendSystemMessage(message("powertool.creative_block_break_undo.success", snapshot));
    }

    public static void clear(ServerPlayer player) {
        HISTORIES.remove(player.getUUID());
    }

    private static Component message(String key, Snapshot snapshot) {
        BlockPos pos = snapshot.pos();
        String command = "/execute in " + snapshot.dimension().identifier() + " run tp @s " + (pos.getX() + 0.5) + " " + (pos.getY() + 1) + " " + (pos.getZ() + 0.5);
        Component coordinates = Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.RunCommand(command))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("powertool.creative_block_break_undo.tp"))));
        return Component.translatable(key, coordinates);
    }

    public record Snapshot(ResourceKey<Level> dimension, BlockPos pos, BlockState state, ItemStack itemStack) {
    }
}
