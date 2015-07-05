package core.framework.impl.cache;

import core.framework.api.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.Duration;

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
    public void put(String name, String key, String value, Duration duration) {
        String redisKey = cacheKey(name, key);
        try {
            redis.setExpire(redisKey, value, duration);
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
