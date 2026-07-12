package org.teacon.powertool.client.anvilcraft.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL46;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * @author ZhuRuoLing
 */
@EventBusSubscriber(Dist.CLIENT)
public class CacheableBERenderingPipeline {
    private static final Logger log = LoggerFactory.getLogger(CacheableBERenderingPipeline.class);
    @Nullable
    private static CacheableBERenderingPipeline instance;
    private final ClientLevel level;
    private final Queue<Runnable> pendingCompiles = new ArrayDeque<>();
    private final Queue<Runnable> pendingUploads = new ArrayDeque<>();
    private final Map<ChunkPos, CachedChunk> regions = new HashMap<>();
    private boolean valid = true;
    private static Vec3 cameraOldPosition = null;
    private static boolean cameraMoved = true;

    private static int glMaxLabelLength = 0;

    public static void create() {
        if (GL.getCapabilities().GL_KHR_debug) {
            glMaxLabelLength = GL46.glGetInteger(GL46.GL_MAX_LABEL_LENGTH);
            glMaxLabelLength /= 2;
        }
    }

    public CachedChunk getRenderRegion(ChunkPos chunkPos) {
        mayWarnForNonRenderThread();
        synchronized (regions) {
            CachedChunk cachedChunk = regions.get(chunkPos);
            if (cachedChunk != null) {
                return cachedChunk;
            }
            CachedChunk region = new CachedChunk(chunkPos, this);
            regions.put(chunkPos, region);
            return region;
        }
    }

    public CacheableBERenderingPipeline(ClientLevel level) {
        this.level = level;
    }

    public void runTasks() {
        while (!pendingCompiles.isEmpty() && valid) {
            pendingCompiles.poll().run();
        }
        while (!pendingUploads.isEmpty() && valid) {
            pendingUploads.poll().run();
        }
    }

    /**
     * Updates the rendering pipeline instance with a new level context.
     *
     * @param level The new ClientLevel instance that the rendering pipeline should be updated to use.
     */
    public static void updateLevel(ClientLevel level) {
        if (instance != null) {
            instance.releaseBuffers();
        }
        instance = new CacheableBERenderingPipeline(level);
    }

    /**
     * Notifies the pipeline that a {@link BlockEntity} has been removed.
     * This method will be automatically called when a {@link BlockEntity} has been removed.
     *
     * @param be The removed {@link BlockEntity}
     */
    public void blockRemoved(BlockEntity be) {
        IBlockEntityRendererExtension<?> renderer = Minecraft.getInstance()
            .getBlockEntityRenderDispatcher()
            .getRenderer(be);
        if (renderer == null) return;
        ChunkPos chunkPos = ChunkPos.containing(be.getBlockPos());
        getRenderRegion(chunkPos).blockRemoved(be);
    }

    public void updateFromNetwork(ChunkPos chunkPos, Collection<BlockPos> entityPos) {
        getRenderRegion(chunkPos).replaceData(entityPos, level);
    }

    /**
     * Notifies the pipeline that a {@link BlockEntity} has been updated and the cache should be rebuilt.
     *
     * @param be The updated {@link BlockEntity}
     */
    public void update(BlockEntity be) {
        BlockEntityRenderer<?, ?> renderer = Minecraft.getInstance()
            .getBlockEntityRenderDispatcher()
            .getRenderer(be);
        if (renderer == null) return;
        ChunkPos chunkPos = ChunkPos.containing(be.getBlockPos());
        getRenderRegion(chunkPos).update(be);
    }

    public void submitUploadTask(Runnable task) {
        pendingUploads.add(task);
    }

    public void submitCompileTask(Runnable task) {
        pendingCompiles.add(task);
    }

    /**
     * Releases all buffers in use and mark current pipeline instance as invalid.
     */
    public void releaseBuffers() {
        mayWarnForNonRenderThread();
        synchronized (regions) {
            regions.values().forEach(CachedChunk::releaseBuffers);
            valid = false;
        }
    }

    public void render(Frustum frustum, boolean translucent) {
        mayWarnForNonRenderThread();
        synchronized (regions) {
            for (CachedChunk value : regions.values()) {
                value.render(frustum, translucent);
            }
        }
    }

    /**
     * Retrieves the current instance of the CacheableBERenderingPipeline.
     *
     * @return The current instance of the CacheableBERenderingPipeline,
     * or null if there has no {@link ClientLevel} in current {@link Minecraft} client.
     */
    @Nullable
    public static CacheableBERenderingPipeline getInstance() {
        return instance;
    }

    public void forcedUpdate(BlockPos pos) {
        getRenderRegion(ChunkPos.containing(pos)).forcedUpdate();
    }

    public static boolean isCameraMoved() {
        return cameraMoved;
    }

    @SubscribeEvent
    public static void on(RenderLevelStageEvent.AfterSky event) {
        Vec3 pos = event.getLevelRenderState().cameraRenderState.pos;
        if (pos.equals(cameraOldPosition)) {
            cameraMoved = false;
            return;
        }
        cameraOldPosition = new Vec3(pos.x, pos.y, pos.z);
        cameraMoved = true;
    }

    public String truncateName(String s) {
        return StringUtil.truncateStringIfNecessary(s, glMaxLabelLength, true);
    }

    private static void mayWarnForNonRenderThread() {
        if (!FMLEnvironment.isProduction() && !RenderSystem.isOnRenderThread()) {
            log.warn("CacheableBERenderingPipeline called from wrong thread!");
        }
    }

    public void forcedUpdate() {
        for (CachedChunk value : regions.values()) {
            value.forcedUpdate();
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (getInstance() == null) return;
        if (event.getLevel() instanceof ServerLevel) return;
        mayWarnForNonRenderThread();
        synchronized (getInstance().regions) {
            ChunkPos chunkPos = event.getChunk().getPos();
            CachedChunk removed = getInstance().regions.remove(chunkPos);
            if (removed != null) {
                removed.releaseBuffers();
            }
        }
    }

    @SubscribeEvent
    public static void on(RenderFrameEvent.Pre event) {
        if (instance != null) {
            instance.handleIntegration();
        }
    }

    private void handleIntegration() {
        // intentionally empty
    }
}
