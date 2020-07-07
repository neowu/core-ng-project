package core.framework.internal.cache;

import core.framework.internal.json.JSONMapper;
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
    private final LocalCacheStore localCache;
    private final RedisCacheStore redisCache;
    private final RedisImpl redis;
    private final JSONMapper<InvalidateLocalCacheMessage> mapper;

    public RedisLocalCacheStore(LocalCacheStore localCache, RedisCacheStore redisCache, RedisImpl redis, JSONMapper<InvalidateLocalCacheMessage> mapper) {
        this.localCache = localCache;
        this.redisCache = redisCache;
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public byte[] get(String key) {
        byte[] value = localCache.get(key);
        if (value != null) return value;
        value = redisCache.get(key);
        if (value == null) return null;
        long expirationTime = redis.expirationTime(key)[0];
        if (expirationTime <= 0) return null;
        localCache.put(key, value, Duration.ofMillis(expirationTime));
        return value;
    }

    @Override
    public Map<String, byte[]> getAll(String... keys) {
        Map<String, byte[]> results = Maps.newHashMapWithExpectedSize(keys.length);
        List<String> localNotFoundKeys = new ArrayList<>();
        for (String key : keys) {
            byte[] value = localCache.get(key);
            if (value != null) {
                results.put(key, value);
            } else {
                localNotFoundKeys.add(key);
            }
        }
        if (!localNotFoundKeys.isEmpty()) {
            Map<String, byte[]> redisValues = redisCache.getAll(localNotFoundKeys.toArray(String[]::new));
            String[] redisKeys = redisValues.keySet().toArray(String[]::new);
            long[] expirationTimes = redis.expirationTime(redisKeys);
            for (int i = 0; i < expirationTimes.length; i++) {
                long expirationTime = expirationTimes[i];
                if (expirationTime > 0) {
                    String redisKey = redisKeys[i];
                    byte[] value = redisValues.get(redisKey);
                    results.put(redisKey, value);
                    localCache.put(redisKey, value, Duration.ofMillis(expirationTime));
                }
            }
        }
        return results;
    }

    @Override
    public void put(String key, byte[] value, Duration expiration) {
        localCache.put(key, value, expiration);
        redisCache.put(key, value, expiration);
        publishInvalidateLocalCacheMessage(List.of(key));
    }

    @Override
    public void putAll(Map<String, byte[]> values, Duration expiration) {
        localCache.putAll(values, expiration);
        redisCache.putAll(values, expiration);
        publishInvalidateLocalCacheMessage(new ArrayList<>(values.keySet()));
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

    private void publishInvalidateLocalCacheMessage(List<String> keys) {
        var message = new InvalidateLocalCacheMessage();
        message.keys = keys;
        message.clientIP = Network.LOCAL_HOST_ADDRESS;
        redis.publish(CHANNEL_INVALIDATE_CACHE, mapper.toJSON(message));
    }
}
