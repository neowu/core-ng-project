package core.framework.impl.cache;

import core.framework.api.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class LocalCacheStore implements CacheStore {
    private final Logger logger = LoggerFactory.getLogger(LocalCacheStore.class);
    private final Map<String, CacheItem> caches = Maps.newConcurrentHashMap();

    @Override
    public String get(String name, String key) {
        String cacheKey = cacheKey(name, key);
        CacheItem item = caches.get(cacheKey);
        if (item == null) return null;
        if (item.expired(System.currentTimeMillis())) {
            caches.remove(cacheKey);
            return null;
        }
        return item.value;
    }

    @Override
    public Map<String, String> getAll(String name, List<String> keys) {
        Map<String, String> results = Maps.newHashMapWithExpectedSize(keys.size());
        for (String key : keys) {
            String value = get(name, key);
            if (value != null) results.put(key, value);
        }
        return results;
    }

    @Override
    public void put(String name, String key, String value, Duration expiration) {
        long now = System.currentTimeMillis();
        String cacheKey = cacheKey(name, key);
        caches.put(cacheKey, new CacheItem(value, now + expiration.toMillis()));
    }

    @Override
    public void putAll(String name, Map<String, String> values, Duration expiration) {
        values.forEach((key, value) -> put(name, key, value, expiration));
    }

    @Override
    public void delete(String name, String key) {
        String cacheKey = cacheKey(name, key);
        caches.remove(cacheKey);
    }

    private String cacheKey(String name, String key) {
        return name + ":" + key;
    }

    void cleanup() {
        logger.debug("clean up expired cache items");
        long now = System.currentTimeMillis();
        caches.forEach((key, value) -> {
            if (value.expired(now)) caches.remove(key);
        });
    }

    public static class CacheItem {
        final String value;
        final long expirationTime;

        public CacheItem(String value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public boolean expired(long now) {
            return now >= expirationTime;
        }
    }
}
