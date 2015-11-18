package core.framework.impl.cache;

import core.framework.api.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
