package org.teacon.powertool.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.entity.exhibit.ExhibitionEntity;
import org.teacon.powertool.network.client.OpenExhibitionEntityEditor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExhibitionEntityEditor extends Item {

    public ExhibitionEntityEditor(final Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResult interactLivingEntity(
            final ItemStack         itemStack,
            final Player            player,
            final LivingEntity      target,
            final InteractionHand   type
    ) {
        final var held = player.getItemInHand(type);

        if (!player.isCreative() || !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return InteractionResult.FAIL;
        }

        if (target instanceof ExhibitionEntity entity) {

            if (player.level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            held.set(PowerToolDataComponents.EDITING_ENTITY, entity.getUUID());
            held.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

            if (!player.isShiftKeyDown()) {
                PacketDistributor.sendToPlayer(
                        (ServerPlayer) player,
                        OpenExhibitionEntityEditor.of(entity)
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public InteractionResult use(Level level, Player player, InteractionHand hand) {

        if (!player.isCreative()
                || !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return InteractionResult.FAIL;
        }

        final var held = player.getItemInHand(hand);

        if (!held.has(PowerToolDataComponents.EDITING_ENTITY)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        final var uuid      = held.get(PowerToolDataComponents.EDITING_ENTITY);

        if (player.isShiftKeyDown()) {
            held.remove(PowerToolDataComponents.EDITING_ENTITY);
            held.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
            return InteractionResult.SUCCESS;
        }

        final var entity    = level.getEntity(uuid);

        if (!(entity instanceof ExhibitionEntity target)) {
            return InteractionResult.PASS;
        }

        PacketDistributor.sendToPlayer(
                (ServerPlayer) player,
                OpenExhibitionEntityEditor.of(target)
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClickEntity(
            final ItemStack stack,
            final Player player,
            final Entity entity
    ) {

        if (!player.isCreative()
                || !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
                || !player.isShiftKeyDown()
        ) {
            return false;
        }

        if (!(entity instanceof ExhibitionEntity exhibition)) {
            return false;
        }

        if (player.level().isClientSide) {
            return false;
        }

        final var editing = stack.get(PowerToolDataComponents.EDITING_ENTITY);

        if (editing == null || !editing.equals(exhibition.getUUID())) {
            stack.set(PowerToolDataComponents.EDITING_ENTITY, exhibition.getUUID());
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(PowerToolDataComponents.EDITING_ENTITY);
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
            entity.remove(Entity.RemovalReason.KILLED);
        }

        return true;
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
                Component.translatable("tooltip.powertool.exhibition_entity_editor1")
                        .withStyle(ChatFormatting.GRAY)
        );
        builder.accept(
                Component.translatable("tooltip.powertool.exhibition_entity_editor2")
                        .withStyle(ChatFormatting.GRAY)
        );
        builder.accept(
                Component.translatable("tooltip.powertool.exhibition_entity_editor3")
                        .withStyle(ChatFormatting.GRAY)
        );
        builder.accept(
                Component.translatable("tooltip.powertool.exhibition_entity_editor4")
                        .withStyle(ChatFormatting.GRAY)
        );
        builder.accept(
                Component.translatable("tooltip.powertool.exhibition_entity_editor5")
                        .withStyle(ChatFormatting.GRAY)
        );
    }
}
