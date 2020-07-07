package core.framework.internal.cache;

import core.framework.internal.log.filter.ArrayLogParam;
import core.framework.internal.log.filter.BytesLogParam;
import core.framework.internal.log.filter.BytesValueMapLogParam;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author neo
 */
public class LocalCacheStore implements CacheStore {
    final Map<String, CacheItem> caches = Maps.newConcurrentHashMap();
    private final Logger logger = LoggerFactory.getLogger(LocalCacheStore.class);
    public long maxSize = (long) (Runtime.getRuntime().maxMemory() * 0.1);  // use 10% of heap by default

    @Override
    public byte[] get(String key) {
        logger.debug("get, key={}", key);
        CacheItem item = caches.get(key);
        if (item == null) return null;
        if (item.expired(System.currentTimeMillis())) {
            caches.remove(key);
            return null;
        }
        item.hits++;
        return item.value;
    }

    @Override
    public Map<String, byte[]> getAll(String... keys) {
        logger.debug("getAll, keys={}", new ArrayLogParam(keys));
        Map<String, byte[]> results = Maps.newHashMapWithExpectedSize(keys.length);
        for (String key : keys) {
            byte[] value = get(key);
            if (value != null) results.put(key, value);
        }
        return results;
    }

    @Override
    public void put(String key, byte[] value, Duration expiration) {
        logger.debug("put, key={}, value={}, expiration={}", key, new BytesLogParam(value), expiration);
        long now = System.currentTimeMillis();
        caches.put(key, new CacheItem(value, now + expiration.toMillis()));
    }

    @Override
    public void putAll(Map<String, byte[]> values, Duration expiration) {
        logger.debug("putAll, values={}, expiration={}", new BytesValueMapLogParam(values), expiration);
        values.forEach((key, value) -> put(key, value, expiration));
    }

    @Override
    public boolean delete(String... keys) {
        logger.debug("delete, keys={}", new ArrayLogParam(keys));
        boolean result = false;
        for (String key : keys) {
            CacheItem previous = caches.remove(key);
            if (!result && previous != null) result = true;
        }
        return result;
    }

    public void cleanup() {    // cleanup is only called by background thread with fixed delay, not need to synchronize
        logger.info("clean up local cache store");
        long now = System.currentTimeMillis();
        long currentSize = 0;
        for (Iterator<CacheItem> iterator = caches.values().iterator(); iterator.hasNext(); ) {
            CacheItem item = iterator.next();
            if (item.expired(now)) {
                iterator.remove();
            } else {
                currentSize += item.value.length;
            }
        }
        if (currentSize > maxSize) {
            logger.info("evict least frequently used cache items");
            long evictSize = currentSize - maxSize;
            evictLeastFrequentlyUsedItems(evictSize);
        }
    }

    // use LFU to trade off between simplicity and access efficiency,
    // assume local cache is only used for rarely changed items, and tolerate stale data, or use message to notify updates
    // cleanup is running in background thread, it maintains approximate maxSize loosely
    private void evictLeastFrequentlyUsedItems(long evictSize) {
        Map<Integer, Long> sizes = new TreeMap<>();
        for (CacheItem item : caches.values()) {
            int hits = item.hits;
            Long size = sizes.getOrDefault(hits, 0L);
            sizes.put(hits, size + item.value.length);
        }
        int minHits = 0;
        long targetEvictSize = evictSize;
        for (Map.Entry<Integer, Long> entry : sizes.entrySet()) {
            targetEvictSize -= entry.getValue();
            if (targetEvictSize <= 0) {
                minHits = entry.getKey();
                break;
            }
        }
        for (Iterator<CacheItem> iterator = caches.values().iterator(); iterator.hasNext(); ) {
            CacheItem item = iterator.next();
            if (item.hits <= minHits) {
                iterator.remove();
            }
        }
    }

    public void flushAll() {
        caches.clear();
    }

    static class CacheItem {
        final byte[] value;
        final long expirationTime;
        int hits;

        CacheItem(byte[] value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        boolean expired(long now) {
            return now >= expirationTime;
        }
    }
}
