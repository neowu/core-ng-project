package core.framework.impl.cache;

import core.framework.api.util.Maps;
import core.framework.impl.redis.RedisImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public class RedisCacheStore implements CacheStore {
    private final Logger logger = LoggerFactory.getLogger(RedisCacheStore.class);

    private final RedisImpl redis;

    public RedisCacheStore(RedisImpl redis) {
        this.redis = redis;
    }

    @Override
    public byte[] get(String key) {
        try {
            return redis.getBytes(key);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<String, byte[]> getAll(String[] keys) {
        try {
            return redis.mgetBytes(keys);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
            return Maps.newHashMap();
        }
    }

    @Override
    public void put(String key, byte[] value, Duration expiration) {
        try {
            redis.set(key, value, expiration);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
        }
    }

    @Override
    public void putAll(Map<String, byte[]> values, Duration expiration) {
        try {
            redis.mset(values, expiration);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            redis.del(key);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
        }
    }
}
