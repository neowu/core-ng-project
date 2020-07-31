package core.framework.internal.cache;

import core.framework.internal.json.JSONWriter;
import core.framework.internal.redis.RedisImpl;
import core.framework.util.Maps;
import core.framework.util.Network;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class RedisLocalCacheStore implements CacheStore {
    public static final String CHANNEL_INVALIDATE_CACHE = "cache:invalidate";
    private final CacheStore localCache;
    private final CacheStore redisCache;
    private final RedisImpl redis;
    private final JSONWriter<InvalidateLocalCacheMessage> writer = new JSONWriter<>(InvalidateLocalCacheMessage.class);

    public RedisLocalCacheStore(CacheStore localCache, CacheStore redisCache, RedisImpl redis) {
        this.localCache = localCache;
        this.redisCache = redisCache;
        this.redis = redis;
    }

    @Override
    public <T> T get(String key, CacheContext<T> context) {
        T value = localCache.get(key, context);
        if (value != null) return value;
        value = redisCache.get(key, context);
        if (value == null) return null;
        long expirationTime = redis.expirationTime(key)[0];
        if (expirationTime <= 0) return null;
        localCache.put(key, value, Duration.ofMillis(expirationTime), context);
        return value;
    }

    @Override
    public <T> Map<String, T> getAll(String[] keys, CacheContext<T> context) {
        Map<String, T> results = Maps.newHashMapWithExpectedSize(keys.length);
        List<String> localNotFoundKeys = new ArrayList<>();
        for (String key : keys) {
            T value = localCache.get(key, context);
            if (value != null) {
                results.put(key, value);
            } else {
                localNotFoundKeys.add(key);
            }
        }
        if (localNotFoundKeys.isEmpty()) return results;

        Map<String, T> redisValues = redisCache.getAll(localNotFoundKeys.toArray(String[]::new), context);
        if (!redisValues.isEmpty()) {
            String[] redisKeys = redisValues.keySet().toArray(String[]::new);
            long[] expirationTimes = redis.expirationTime(redisKeys);
            for (int i = 0; i < expirationTimes.length; i++) {
                long expirationTime = expirationTimes[i];
                if (expirationTime > 0) {
                    String redisKey = redisKeys[i];
                    T value = redisValues.get(redisKey);
                    results.put(redisKey, value);
                    localCache.put(redisKey, value, Duration.ofMillis(expirationTime), context);
                }
            }
        }
        return results;
    }

    @Override
    public <T> void put(String key, T value, Duration expiration, CacheContext<T> context) {
        localCache.put(key, value, expiration, context);
        redisCache.put(key, value, expiration, context);
        publishInvalidateLocalCacheMessage(List.of(key));
    }

    @Override
    public <T> void putAll(List<Entry<T>> values, Duration expiration, CacheContext<T> context) {
        localCache.putAll(values, expiration, context);
        redisCache.putAll(values, expiration, context);
        publishInvalidateLocalCacheMessage(keys(values));
    }

    @Override
    public boolean delete(String... keys) {
        boolean deleted = redisCache.delete(keys);
        localCache.delete(keys);
        if (deleted) {
            publishInvalidateLocalCacheMessage(Arrays.asList(keys));
        }
        return deleted;
    }

    private <T> List<String> keys(List<Entry<T>> values) {
        List<String> keys = new ArrayList<>(values.size());
        for (Entry<T> value : values) {
            keys.add(value.key);
        }
        return keys;
    }

    private void publishInvalidateLocalCacheMessage(List<String> keys) {
        var message = new InvalidateLocalCacheMessage();
        message.keys = keys;
        message.clientIP = Network.LOCAL_HOST_ADDRESS;
        redis.publish(CHANNEL_INVALIDATE_CACHE, writer.toJSON(message));
    }
}
