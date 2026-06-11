package org.teacon.powertool.client.renders;

import com.mojang.blaze3d.pipeline.TextureTarget;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class RecipeLayoutCache {

    public static final int DEFAULT_MAX_SIZE = 8;

    private final int maxSize;
    private final Map<CacheKey, CacheEntry> entries = new LinkedHashMap<>();
    private final Set<CacheKey> usedThisFrame = new HashSet<>();
    private final Set<CacheKey> protectedKeys = new HashSet<>();
    private final Set<CacheKey> dirtyKeys = new HashSet<>();

    public record CacheKey(Identifier recipeType, Identifier recipeId) {}

    public record CacheEntry(IRecipeLayoutDrawable<?> layout, TextureTarget textureTarget) {}

    public RecipeLayoutCache() {
        this(DEFAULT_MAX_SIZE);
    }

    public RecipeLayoutCache(int maxSize) {
        this.maxSize = maxSize;
    }

    @Nullable
    public CacheEntry getOrCreate(@Nullable Identifier recipeType, @Nullable Identifier recipeId, Supplier<IRecipeLayoutDrawable<?>> creator) {
        if (recipeType == null || recipeId == null) {
            return null;
        }
        var key = new CacheKey(recipeType, recipeId);
        var entry = entries.get(key);
        if (entry == null) {
            var layout = creator.get();
            if (layout == null) {
                return null;
            }
            var rect = layout.getRectWithBorder();
            var target = new TextureTarget(null, rect.getWidth(), rect.getHeight(), true);
            entry = new CacheEntry(layout, target);
            entries.put(key, entry);
            dirtyKeys.add(key);
        }
        usedThisFrame.add(key);
        return entry;
    }

    @Nullable
    public CacheEntry get(@Nullable Identifier recipeType, @Nullable Identifier recipeId) {
        if (recipeType == null || recipeId == null) {
            return null;
        }
        return entries.get(new CacheKey(recipeType, recipeId));
    }

    public Set<CacheKey> getDirtyKeys() {
        return dirtyKeys;
    }

    public void endFrame() {
        protectedKeys.clear();
        protectedKeys.addAll(usedThisFrame);
        usedThisFrame.clear();
        int total = entries.size();
        if (total > maxSize) {
            var iter = entries.entrySet().iterator();
            while (iter.hasNext() && entries.size() > maxSize) {
                var next = iter.next();
                if (!protectedKeys.contains(next.getKey())) {
                    next.getValue().textureTarget.destroyBuffers();
                    dirtyKeys.remove(next.getKey());
                    iter.remove();
                }
            }
        }
    }
}
