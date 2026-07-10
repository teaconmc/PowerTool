package org.teacon.powertool.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.teacon.powertool.entity.exhibit.ExhibitionEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExhibitionEntitySpawner extends Item {

    public ExhibitionEntitySpawner(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final var player    = context.getPlayer();

        if (player == null || !player.isCreative()) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        final var held      = context.getItemInHand();
        final var pos       = context.getClickedPos();
        final var face      = context.getClickedFace();
        final var block     = level.getBlockState(pos);

        BlockPos spawnPos;
        if (block.getCollisionShape(level, pos).isEmpty()) {
            spawnPos    = pos;
        } else {
            spawnPos    = pos.relative(face);
        }

        final var type  = SpawnEggItem.getType(held);
        if (type == null) {
            return InteractionResult.FAIL;
        }

        final var spawn = type.spawn(
                serverLevel,
                held,
                player,
                spawnPos,
                EntitySpawnReason.SPAWN_ITEM_USE,
                true,
                !Objects.equals(pos, spawnPos) && face == Direction.UP
        );

        if (spawn instanceof ExhibitionEntity exhibition) {

            final var node = held.get(PowerToolDataComponents.EXHIBITION_NODES);

            held.consume(1, player);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);

            if (node != null) {
                exhibition.init(node);
            }

        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(
            final Level level,
            final Player player,
            final InteractionHand hand
    ) {

        if (!player.isCreative() || !player.isShiftKeyDown()) {
            return InteractionResult.FAIL;
        }

        final var held  = player.getItemInHand(hand);

        if (!held.has(PowerToolDataComponents.EXHIBITION_NODES)) {
            return InteractionResult.FAIL;
        }

        if (!player.level().isClientSide()) {
            held.remove(PowerToolDataComponents.EXHIBITION_NODES);
            held.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(
            final ItemStack itemStack,
            final Player player,
            final LivingEntity target,
            final InteractionHand type
    ) {

        if (!player.isCreative() || !player.isShiftKeyDown()) {
            return InteractionResult.FAIL;
        }

        final var held = player.getItemInHand(type);

        if (!(target instanceof ExhibitionEntity exhibition)) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide()) {
            held.set(PowerToolDataComponents.EXHIBITION_NODES, exhibition.getExhibitionNode().toImmutable());
            held.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            final ItemStack itemStack,
            final TooltipContext context,
            final TooltipDisplay display,
            final Consumer<Component> builder,
            final TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
        builder.accept(
                Component.translatable("tooltip.powertool.exhibition_entity_spawner1")
                        .withStyle(ChatFormatting.GRAY)
        );
        builder.accept(
                Component.translatable("tooltip.powertool.exhibition_entity_spanwer2")
                        .withStyle(ChatFormatting.GRAY)
        );
    }
}
