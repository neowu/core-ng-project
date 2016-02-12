package core.framework.impl.cache;

import core.framework.api.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public class LocalCacheStore implements CacheStore {
    private final Logger logger = LoggerFactory.getLogger(LocalCacheStore.class);
    private final Map<String, CacheItem> caches = Maps.newConcurrentHashMap();

    @Override
    public byte[] get(String key) {
        CacheItem item = caches.get(key);
        if (item == null) return null;
        if (item.expired(System.currentTimeMillis())) {
            caches.remove(key);
            return null;
        }
        return item.value;
    }

    @Override
    public Map<String, byte[]> getAll(String[] keys) {
        Map<String, byte[]> results = Maps.newHashMapWithExpectedSize(keys.length);
        for (String key : keys) {
            byte[] value = get(key);
            if (value != null) results.put(key, value);
        }
        return results;
    }

    @Override
    public void put(String key, byte[] value, Duration expiration) {
        long now = System.currentTimeMillis();
        caches.put(key, new CacheItem(value, now + expiration.toMillis()));
    }

    @Override
    public void putAll(Map<String, byte[]> values, Duration expiration) {
        values.forEach((key, value) -> put(key, value, expiration));
    }

    @Override
    public void delete(String key) {
        caches.remove(key);
    }

    public void cleanup() {
        logger.info("clean up local cache store");
        long now = System.currentTimeMillis();
        caches.forEach((key, value) -> {
            if (value.expired(now)) caches.remove(key);
        });
    }

    public static class CacheItem {
        final byte[] value;
        final long expirationTime;

        public CacheItem(byte[] value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public boolean expired(long now) {
            return now >= expirationTime;
        }
    }
}
