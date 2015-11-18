package core.framework.impl.cache;

import core.framework.api.redis.Redis;
import core.framework.api.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class RedisCacheStore implements CacheStore {
    private final Logger logger = LoggerFactory.getLogger(RedisCacheStore.class);

    private final Redis redis;

    public RedisCacheStore(Redis redis) {
        this.redis = redis;
    }

    @Override
    public String get(String name, String key) {
        String redisKey = cacheKey(name, key);
        try {
            return redis.get(redisKey);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<String> getAll(String name, List<String> keys) {
        int size = keys.size();
        List<String> redisKeys = new ArrayList<>(size);
        for (String key : keys) {
            redisKeys.add(cacheKey(name, key));
        }
        try {
            return redis.mget(redisKeys);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
            List<String> results = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                results.add(null);
            }
            return results;
        }
    }

    @Override
    public void put(String name, String key, String value, Duration expiration) {
        String redisKey = cacheKey(name, key);
        try {
            redis.set(redisKey, value, expiration);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
        }
    }

    @Override
    public void putAll(String name, Map<String, String> values, Duration expiration) {
        Map<String, String> redisValues = Maps.newHashMapWithExpectedSize(values.size());
        for (Map.Entry<String, String> entry : values.entrySet()) {
            redisValues.put(cacheKey(name, entry.getKey()), entry.getValue());
        }
        try {
            redis.mset(redisValues);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
        }
    }

    @Override
    public void delete(String name, String key) {
        String redisKey = cacheKey(name, key);
        try {
            redis.del(redisKey);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
        }
    }

    private String cacheKey(String name, String key) {
        return name + ":" + key;
    }
}
