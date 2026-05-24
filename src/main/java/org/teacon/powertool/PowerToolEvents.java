package org.teacon.powertool;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import org.teacon.powertool.annotation.NonNullByDefault;
import org.teacon.powertool.attachment.PowerToolAttachments;
import org.teacon.powertool.entity.AutoVanishBoat;
import org.teacon.powertool.entity.AutoVanishMinecart;
import org.teacon.powertool.network.client.UpdateCachedModeChunkDataPacket;
import org.teacon.powertool.network.client.UpdateDisplayChunkDataPacket;
import org.teacon.powertool.network.client.UpdateStaticModeChunkDataPacket;
import org.teacon.powertool.utils.DelayServerExecutor;
import org.teacon.powertool.utils.VanillaUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NonNullByDefault
@EventBusSubscriber(modid = PowerTool.MODID)
public class PowerToolEvents {
    
    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        ChunkPos chunkPos = event.getPos();
        var listDisplayModeData = event.getChunk().getData(PowerToolAttachments.DISPLAY_MODE);
        var listStaticModeData = event.getChunk().getData(PowerToolAttachments.STATIC_MODE);
        var listCachedModeData = event.getChunk().getData(PowerToolAttachments.CACHED_MODE);
        PacketDistributor.sendToPlayer(event.getPlayer(), new UpdateDisplayChunkDataPacket(chunkPos.x(), chunkPos.z(), listDisplayModeData));
        PacketDistributor.sendToPlayer(event.getPlayer(), new UpdateStaticModeChunkDataPacket(chunkPos.x(), chunkPos.z(), listStaticModeData));
        PacketDistributor.sendToPlayer(event.getPlayer(), new UpdateCachedModeChunkDataPacket(chunkPos.x(), chunkPos.z(), listCachedModeData));
    }
    
    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(VanillaUtils.isBlockStaticMode(event.getEntity(), event.getPos()));
    }
    
    @SubscribeEvent
    public static void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        event.setCanceled(VanillaUtils.isBlockStaticMode(event.getEntity(), event.getPos()));
    }
    
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Map<ChunkPos, List<BlockPos>> map = new HashMap<>();
        for (BlockPos affectedBlock : event.getAffectedBlocks()) {
            map.computeIfAbsent(ChunkPos.containing(affectedBlock), it -> new ArrayList<>())
                    .add(affectedBlock);
        }
        map.entrySet()
                .stream()
                .map(it -> Map.entry(
                        Map.entry(
                                event.getLevel().getChunk(it.getKey().x(), it.getKey().z()),
                                it.getKey()
                        ),
                        it.getValue()
                ))
                .collect(Util.toMap())
                .forEach((entry, list) -> {
                    LevelChunk chunk = entry.getKey();
                    ChunkPos pos = entry.getValue();
                    for (BlockPos blockPos : list) {
                        removeAccessControl((ServerLevel) event.getLevel(), pos, chunk, blockPos, null);
                        removeCachedModeControl((ServerLevel) event.getLevel(), pos, chunk, blockPos, null);
                    }
                });
    }
    
    private static void removeCachedModeControl(
            ServerLevel level,
            ChunkPos chunkPos,
            ChunkAccess chunk,
            BlockPos pos,
            @Nullable ServerPlayer player
    ) {
        var cachedEnabledPosList = new ArrayList<>(chunk.getData(PowerToolAttachments.CACHED_MODE));
        cachedEnabledPosList.remove(pos);
        chunk.setData(PowerToolAttachments.CACHED_MODE, cachedEnabledPosList);
        chunk.markUnsaved();
        var packet = new UpdateCachedModeChunkDataPacket(chunkPos.x(), chunkPos.z(), cachedEnabledPosList);
        if (player == null) {
            PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, packet);
            return;
        }
        PacketDistributor.sendToPlayer(player, packet);
    }
    
    private static void removeAccessControl(
            ServerLevel level,
            ChunkPos chunkPos,
            ChunkAccess chunk,
            BlockPos pos,
            @Nullable ServerPlayer player
    ) {
        var displayEnabledPosList = new ArrayList<>(chunk.getData(PowerToolAttachments.DISPLAY_MODE));
        displayEnabledPosList.remove(pos);
        var staticEnabledPosList = new ArrayList<>(chunk.getData(PowerToolAttachments.STATIC_MODE));
        staticEnabledPosList.remove(pos);
        chunk.setData(PowerToolAttachments.DISPLAY_MODE, displayEnabledPosList);
        chunk.setData(PowerToolAttachments.STATIC_MODE, staticEnabledPosList);
        chunk.markUnsaved();
        var displayModePacket = new UpdateDisplayChunkDataPacket(chunkPos.x(), chunkPos.z(), displayEnabledPosList);
        var staticModePacket = new UpdateStaticModeChunkDataPacket(chunkPos.x(), chunkPos.z(), staticEnabledPosList);
        if (player == null) {
            PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, displayModePacket);
            PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, staticModePacket);
            return;
        }
        PacketDistributor.sendToPlayer(player, displayModePacket);
        PacketDistributor.sendToPlayer(player, staticModePacket);
    }
    
    private static void removeAccessControl(
            ServerLevel level,
            BlockPos pos,
            @Nullable ServerPlayer player
    ) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        ChunkAccess chunk = level.getChunk(pos);
        removeAccessControl(level, chunkPos, chunk, pos, player);
        removeCachedModeControl(level, chunkPos, chunk, pos, player);
    }
    
    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if(event.getLevel() instanceof ServerLevel level){
            BlockPos pos = event.getPos();
            removeAccessControl(level, pos, (ServerPlayer) event.getPlayer());
        }
    }
    
    @SubscribeEvent
    public static void onChangeDimension(EntityTravelToDimensionEvent event) {
        if (event.getDimension().equals(ServerLevel.END) && PowerToolConfig.disableTeleportToEnd.get()) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onAddEntity(EntityJoinLevelEvent event) {
        var entity = event.getEntity();
        var level = event.getLevel();
        if (PowerToolConfig.vehicleAutoVanish.get()) {
            if (entity instanceof AbstractBoat boat && !(entity instanceof AutoVanishBoat)) {
                var newBoat = AutoVanishBoat.fromBoat(boat);
                DelayServerExecutor.addTask(2, (server) -> level.addFreshEntity(newBoat));
                event.setCanceled(true);
            }
            if (entity.getClass().equals(Minecart.class)) {
                var newMinecart = AutoVanishMinecart.fromMinecart((Minecart) entity);
                DelayServerExecutor.addTask(2, (server) -> level.addFreshEntity(newMinecart));
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CreativeNoClip.sync(player);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CreativeNoClip.sync(player);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CreativeNoClip.sync(player);
        }
    }
}
