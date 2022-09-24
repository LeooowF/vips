package io.github.leooowf.vips.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.leooowf.vips.VipsPlugin;
import io.github.leooowf.vips.model.Key;

public class KeysCache {

    private final Cache<String, Key> keysCache = Caffeine.newBuilder().build();

    public void put(Key key) {
        keysCache.put(key.getId(), key);
    }

    public void invalidate(String id) {
        keysCache.invalidate(id);
    }

    public Key find(String id) {
        return keysCache.getIfPresent(id);
    }

    public void populate() {
        for (Key key : VipsPlugin.getInstance().getKeyDAO().selectAll()) {
            keysCache.put(key.getId(), key);
        }
    }
}