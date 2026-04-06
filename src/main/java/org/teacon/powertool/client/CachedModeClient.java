package org.teacon.powertool.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedModeClient {
    public static final CachedModeClient INSTANCE = new CachedModeClient();
    private final Map<ChunkPos, List<BlockPos>> cachedModeData = new HashMap<>();
    
    public boolean isCachedModeEnabledOn(BlockEntity be) {
        BlockEntityRenderer<?,?> renderer = Minecraft.getInstance()
                .getBlockEntityRenderDispatcher()
                .getRenderer(be);
        if (renderer == null) return false;
        return isCachedModeEnabledOn(be.getBlockPos());
    }
    
    public boolean isCachedModeEnabledOn(BlockPos pos) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        if (!cachedModeData.containsKey(chunkPos)) return false;
        return cachedModeData.get(chunkPos).contains(pos);
    }
    
    public void updateCachedModeData(ChunkPos chunkPos, List<BlockPos> blockPosList) {
        cachedModeData.put(chunkPos, blockPosList);
        //todo
//        CacheableBERenderingPipeline.getInstance().updateFromNetwork(chunkPos, blockPosList);
    }
}
