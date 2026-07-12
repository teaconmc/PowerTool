package org.teacon.powertool.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessControlClient {
    public static final AccessControlClient INSTANCE = new AccessControlClient();
    private final Map<ChunkPos, List<BlockPos>> displayModeData = new HashMap<>();
    private final Map<ChunkPos, List<BlockPos>> staticModeData = new HashMap<>();
    private BlockPos interactionSourcePos = null;
    
    public boolean isDisplayModeEnabledOn(Screen screen) {
        Player player = Minecraft.getInstance().player;
        if (player == null || interactionSourcePos == null || player.getAbilities().instabuild) {
            return false;
        }
        if (screen instanceof AbstractContainerScreen<? extends AbstractContainerMenu> abstractContainerScreen) {
            ChunkPos pos = ChunkPos.containing(interactionSourcePos);
            if (displayModeData.containsKey(pos)) {
                return displayModeData.get(pos).contains(interactionSourcePos);
            }
        }
        return false;
    }
    
    public void screenClosed() {
        interactionSourcePos = null;
    }
    
    public void clear() {
        displayModeData.clear();
        staticModeData.clear();
    }
    
    public void updateInteractionSource(BlockPos pos) {
        this.interactionSourcePos = pos;
    }
    
    public void updateDisplayModeData(ChunkPos chunkPos, List<BlockPos> blockPosList) {
        displayModeData.put(chunkPos, blockPosList);
    }
    
    public void updateStaticModeData(ChunkPos chunkPos, List<BlockPos> blockPosList) {
        staticModeData.put(chunkPos, blockPosList);
    }
    
    public boolean isDisplayModeEnabledAt(BlockPos blockPos) {
        ChunkPos pos = ChunkPos.containing(blockPos);
        List<BlockPos> list = displayModeData.get(pos);
        if (list != null) {
            return list.contains(blockPos);
        }
        return false;
    }
    
    public boolean isStaticModeEnabledAt(BlockPos blockPos) {
        ChunkPos pos = ChunkPos.containing(blockPos);
        List<BlockPos> list = staticModeData.get(pos);
        if (list != null) {
            return list.contains(blockPos);
        }
        return false;
    }
}
