package core.framework.impl.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class LocalCacheStore implements CacheStore {
    private final Logger logger = LoggerFactory.getLogger(LocalCacheStore.class);

    private final Map<String, CacheItem> caches = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        scheduler.scheduleWithFixedDelay(this::cleanup, 30, 30, TimeUnit.MINUTES);
        logger.info("local cache cleaner started");
    }

    public void shutdown() {
        logger.info("shutdown local cache cleaner");
        scheduler.shutdown();
    }

    void cleanup() {
        Thread.currentThread().setName("local-cache-cleaner");
        logger.info("clean up expired cache items");
        long now = System.currentTimeMillis();
        caches.forEach((key, value) -> {
            if (value.expired(now)) caches.remove(key);
        });
    }

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
    public void put(String name, String key, String value, Duration duration) {
        long now = System.currentTimeMillis();
        String cacheKey = cacheKey(name, key);
        caches.put(cacheKey, new CacheItem(value, now + duration.toMillis()));
    }

    @Override
    public void delete(String name, String key) {
        String cacheKey = cacheKey(name, key);
        caches.remove(cacheKey);
    }

    private String cacheKey(String name, String key) {
        return name + ":" + key;
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
