package org.teacon.powertool.item;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.client.anvilcraft.rendering.CacheableBERenderingPipeline;
import org.teacon.powertool.network.client.UpdateCachedModeChunkDataPacket;
import org.teacon.powertool.network.client.UpdateDisplayChunkDataPacket;
import org.teacon.powertool.network.client.UpdateStaticModeChunkDataPacket;
import org.teacon.powertool.utils.VanillaUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AccessControlToolItem extends Item {

    private final Type type;

    public AccessControlToolItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(
            Component.translatable(type.tooltipKey)
                .withStyle(ChatFormatting.GRAY)
        );
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        if (type.canInteract(level, pos, player)) {
            type.attackBlock(level, pos, player);
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            return InteractionResult.FAIL;
        }
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!type.canInteract(level, pos, player)) {
            return InteractionResult.FAIL;
        }

        BlockState blockState = level.getBlockState(pos);
        Component blockName = VanillaUtils.getName(blockState.getBlock());
        ChunkPos chunkPos = new ChunkPos(pos);
        ChunkAccess chunk = level.getChunkAt(pos);
        List<BlockPos> enabledPosList = new ArrayList<>(chunk.getData(type.dataType));
        if (enabledPosList.contains(pos)) {
            enabledPosList.remove(pos);
            player.displayClientMessage(
                Component.translatable(type.removeTooltipKey, blockName),
                true
            );
        } else {
            enabledPosList.add(pos);
            player.displayClientMessage(
                Component.translatable(type.addTooltipKey, blockName),
                true
            );
        }
        chunk.setUnsaved(true);
        chunk.setData(type.dataType, enabledPosList);
        PacketDistributor.sendToPlayer((ServerPlayer) player, type.createUpdatePack(chunkPos, enabledPosList));
        return InteractionResult.SUCCESS;
    }

    public enum Type {
        DISPLAY_MODE(
            PowerToolAttachments.DISPLAY_MODE,
            "powertool.gui.display_mode_enabled",
            "powertool.gui.display_mode_disabled",
            "tooltip.powertool.display_tool"
        ) {
            @Override
            public CustomPacketPayload createUpdatePack(ChunkPos chunkPos, List<BlockPos> list) {
                return new UpdateDisplayChunkDataPacket(chunkPos.x, chunkPos.z, list);
            }

            @Override
            public boolean canInteract(Level level, BlockPos pos, Player player) {
                BlockState blockState = level.getBlockState(pos);
                Component blockName = VanillaUtils.getName(blockState.getBlock());
                MenuProvider menuProvider = blockState.getMenuProvider(level, pos);

                if (menuProvider == null && !player.isShiftKeyDown()) {
                    player.displayClientMessage(
                        Component.translatable("powertool.gui.display_mode_error", blockName)
                            .withStyle(ChatFormatting.RED),
                        true
                    );
                    return false;
                }
                return true;
            }
        },
        STATIC_MODE(
            PowerToolAttachments.STATIC_MODE,
            "powertool.gui.static_mode_enabled",
            "powertool.gui.static_mode_disabled",
            "tooltip.powertool.static_tool") {
            @Override
            public CustomPacketPayload createUpdatePack(ChunkPos chunkPos, List<BlockPos> list) {
                return new UpdateStaticModeChunkDataPacket(chunkPos.x, chunkPos.z, list);
            }

            @Override
            public boolean canInteract(Level level, BlockPos pos, Player player) {
                return true;
            }
        },
        CACHED_MODE(
            PowerToolAttachments.CACHED_MODE,
            "powertool.gui.cached_mode_enabled",
            "powertool.gui.cached_mode_disabled",
            "tooltip.powertool.cached_mode_tool"
        ) {
            @Override
            public CustomPacketPayload createUpdatePack(ChunkPos chunkPos, List<BlockPos> list) {
                return new UpdateCachedModeChunkDataPacket(chunkPos.x, chunkPos.z, list);
            }

            @Override
            public boolean canInteract(Level level, BlockPos pos, Player player) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity == null) return false;
                if (!level.isClientSide) return true;
                return Type.isRendererPresent(blockEntity);
            }

            @Override
            public void attackBlock(Level level, BlockPos pos, Player player) {
                if (!level.isClientSide) return;
                player.sendSystemMessage(Component.translatable("powertool.gui.cache_updated"));
                forceUpdate(pos);
            }

            @OnlyIn(Dist.CLIENT)
            private static void forceUpdate(BlockPos pos) {
                CacheableBERenderingPipeline.getInstance().forcedUpdate(pos);
            }
        };

        @OnlyIn(Dist.CLIENT)
        private static boolean isRendererPresent(BlockEntity be) {
            BlockEntityRenderer<?> renderer = Minecraft.getInstance()
                .getBlockEntityRenderDispatcher()
                .getRenderer(be);
            return renderer != null;
        }

        public final Supplier<AttachmentType<List<BlockPos>>> dataType;
        public final String addTooltipKey;
        public final String removeTooltipKey;
        public final String tooltipKey;

        Type(Supplier<AttachmentType<List<BlockPos>>> dataType, String addTooltipKey, String removeTooltipKey, String tooltipKey) {
            this.dataType = dataType;
            this.addTooltipKey = addTooltipKey;
            this.removeTooltipKey = removeTooltipKey;
            this.tooltipKey = tooltipKey;
        }

        public abstract CustomPacketPayload createUpdatePack(ChunkPos chunkPos, List<BlockPos> list);

        public abstract boolean canInteract(Level level, BlockPos pos, Player player);

        public void attackBlock(Level level, BlockPos pos, Player player) {
        }
    }
}
