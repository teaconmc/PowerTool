package org.teacon.powertool.client.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.teacon.powertool.PowerTool;
import org.teacon.powertool.entity.exhibit.ExhibitionHumanoid;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// 暂时找不到其他更好的解决方案
public class ClientExhibitionHumanoid extends ExhibitionHumanoid {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final PlayerSkinRenderCache skinRenderCache;
    private @Nullable CompletableFuture<Optional<PlayerSkin>> skinLookup;
    private PlayerSkin skin;

    public static void registerOverrides(PlayerSkinRenderCache cache) {
        ExhibitionHumanoid.constructor = (type, level)
                -> level instanceof ClientLevel
                ? new ClientExhibitionHumanoid(level, cache)
                : new ExhibitionHumanoid(type, level);
    }

    protected ClientExhibitionHumanoid(
            final Level                             level,
            final PlayerSkinRenderCache             cache
    ) {
        super(level);
        this.skinRenderCache = cache;
        this.skin = ClientMannequin.DEFAULT_SKIN;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.skinLookup != null && this.skinLookup.isDone()) {
            try {
                this.skinLookup.get().ifPresent(this::setSkin);
                this.skinLookup = null;
            } catch (Exception var2) {
                LOGGER.error("Error when trying to look up skin", var2);
            }
        }

    }

    @Override
    public void onSyncedDataUpdated(@NonNull EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (accessor.equals(DATA_NODE)) {
            this.updateSkin();
        }

    }

    private void updateSkin() {
        if (this.skinLookup != null) {
            CompletableFuture<Optional<PlayerSkin>> future = this.skinLookup;
            this.skinLookup = null;
            future.cancel(false);
        }

        if (this.skinRenderCache != null) {
            this.skinLookup = this.skinRenderCache
                    .lookup(this.getProfile())
                    .thenApply((info) -> info.map(PlayerSkinRenderCache.RenderInfo::playerSkin));
        }
    }

    public PlayerSkin getSkin() {
        return this.skin;
    }

    private void setSkin(PlayerSkin skin) {
        this.skin = skin;
    }

    @EventBusSubscriber(modid = PowerTool.MODID, value = Dist.CLIENT)
    private static final class Handler {
        @SubscribeEvent
        public static void onClientStarted(final ClientStartedEvent event) {
            ClientExhibitionHumanoid.registerOverrides(Minecraft.getInstance().playerSkinRenderCache());
        }
    }
}
