package org.teacon.powertool.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.entity.exhibit.ExhibitionEntity;
import org.teacon.powertool.network.client.OpenExhibitionEntityEditor;

import javax.annotation.ParametersAreNonnullByDefault;

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
        if (target instanceof ExhibitionEntity entity) {

            if (player instanceof ServerPlayer) {
                PacketDistributor.sendToPlayer(
                        (ServerPlayer) player,
                        OpenExhibitionEntityEditor.of(entity)
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
