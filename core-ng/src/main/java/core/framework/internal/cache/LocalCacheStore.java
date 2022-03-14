package core.framework.internal.cache;

import core.framework.internal.log.filter.ArrayLogParam;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author neo
 */
public class LocalCacheStore implements CacheStore {
    final Map<String, CacheItem<?>> caches = Maps.newConcurrentHashMap();
    private final Logger logger = LoggerFactory.getLogger(LocalCacheStore.class);
    public int maxSize = 10000;  // 10000 simple objects roughly takes 1M-10M heap + hashmap overhead

    @Override
    public <T> T get(String key, CacheContext<T> context) {
        logger.debug("get, key={}", key);
        return get(key, System.currentTimeMillis());
    }

    private <T> T get(String key, long now) {
        @SuppressWarnings("unchecked")
        CacheItem<T> item = (CacheItem<T>) caches.get(key);
        if (item == null) return null;
        if (item.expired(now)) {
            caches.remove(key);
            return null;
        }
        item.hits++;
        return item.value;
    }

    @Override
    public <T> Map<String, T> getAll(String[] keys, CacheContext<T> context) {
        logger.debug("getAll, keys={}", new ArrayLogParam(keys));
        long now = System.currentTimeMillis();
        Map<String, T> results = Maps.newHashMapWithExpectedSize(keys.length);
        for (String key : keys) {
            T value = get(key, now);
            if (value != null) results.put(key, value);
        }
        return results;
    }

    @Override
    public <T> void put(String key, T value, Duration expiration, CacheContext<T> context) {
        logger.debug("put, key={}, expiration={}", key, expiration);
        long expirationTime = System.currentTimeMillis() + expiration.toMillis();
        caches.put(key, new CacheItem<>(value, expirationTime));
    }

    @Override
    public <T> void putAll(List<Entry<T>> values, Duration expiration, CacheContext<T> context) {
        logger.debug("putAll, keys={}, expiration={}", new ArrayLogParam(keys(values)), expiration);
        long expirationTime = System.currentTimeMillis() + expiration.toMillis();
        for (Entry<T> value : values) {
            caches.put(value.key(), new CacheItem<>(value.value(), expirationTime));
        }
    }

    private <T> String[] keys(List<Entry<T>> values) {
        String[] keys = new String[values.size()];
        int index = 0;
        for (Entry<T> value : values) {
            keys[index] = value.key();
            index++;
        }
        return keys;
    }

    @Override
    public boolean delete(String... keys) {
        logger.debug("delete, keys={}", new ArrayLogParam(keys));
        boolean deleted = false;
        for (String key : keys) {
            CacheItem<?> previous = caches.remove(key);
            if (!deleted && previous != null) deleted = true;
        }
        return deleted;
    }

    public void cleanup() {    // cleanup is only called by background thread with fixed delay, not need to synchronize
        logger.info("clean up local cache store");
        long now = System.currentTimeMillis();
        caches.values().removeIf(item -> item.expired(now));
        int size = caches.size();
        if (size > maxSize) {
            logger.info("evict least frequently used cache items, currentSize={}, maxSize={}", size, maxSize);
            int evictSize = size - maxSize;
            evictLeastFrequentlyUsedItems(evictSize);
        }
    }

    // use LFU to trade off between simplicity and access efficiency,
    // assume local cache is only used for rarely changed items, and tolerate stale data, or use message to notify updates
    // cleanup is running in background thread, it maintains approximate maxSize loosely
    private void evictLeastFrequentlyUsedItems(int evictSize) {
        Map<Integer, Integer> sizes = new TreeMap<>();
        for (CacheItem<?> item : caches.values()) {
            int hits = item.hits;
            int size = sizes.getOrDefault(hits, 0);
            sizes.put(hits, size + 1);
        }
        int minHits = 0;
        int currentEvictSize = 0;
        for (Map.Entry<Integer, Integer> entry : sizes.entrySet()) {
            currentEvictSize += entry.getValue();
            if (currentEvictSize >= evictSize) {
                minHits = entry.getKey();
                break;
            }
        }
        int targetHits = minHits;
        caches.values().removeIf(item -> item.hits <= targetHits);
    }

    public void clear() {
        caches.clear();
    }

    static class CacheItem<T> {
        final T value;
        final long expirationTime;
        int hits;

        CacheItem(T value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        boolean expired(long now) {
            return now >= expirationTime;
        }
    }
}
