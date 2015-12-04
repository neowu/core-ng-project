package core.framework.impl.cache;

import core.framework.api.redis.Redis;
import core.framework.api.util.Maps;
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

    private final Redis redis;

    public RedisCacheStore(Redis redis) {
        this.redis = redis;
    }

    @Override
    public String get(String key) {
        try {
            return redis.get(key);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<String, String> getAll(String[] keys) {
        try {
            return redis.mget(keys);
        } catch (JedisConnectionException e) {
            logger.warn("failed to connect to redis, error={}", e.getMessage(), e);
            return Maps.newHashMap();
        }
    }

    @Override
    public void put(String key, String value, Duration expiration) {
        try {
            redis.set(key, value, expiration);
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
