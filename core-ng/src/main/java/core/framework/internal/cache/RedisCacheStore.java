package core.framework.internal.cache;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.redis.RedisException;
import core.framework.internal.redis.RedisImpl;
import core.framework.internal.validate.Validator;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static core.framework.log.Markers.errorCode;

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
    public <T> T get(String key, JSONMapper<T> mapper, Validator validator) {
        try {
            byte[] value = redis.getBytes(key);
            if (value == null) return null;
            return deserialize(value, mapper, validator);
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public <T> Map<String, T> getAll(String[] keys, JSONMapper<T> mapper, Validator validator) {
        try {
            Map<String, byte[]> redisValues = redis.multiGetBytes(keys);
            Map<String, T> values = Maps.newHashMapWithExpectedSize(redisValues.size());
            for (Map.Entry<String, byte[]> entry : redisValues.entrySet()) {
                T value = deserialize(entry.getValue(), mapper, validator);
                if (value != null) {
                    values.put(entry.getKey(), value);
                }
            }
            return values;
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
            return Map.of();
        }
    }

    private <T> T deserialize(byte[] value, JSONMapper<T> mapper, Validator validator) {
        try {
            T result = mapper.reader.readValue(value);
            if (result == null) return null;

            Map<String, String> errors = validator.errors(result, false);
            if (errors != null) {
                logger.warn(errorCode("INVALID_CACHE_DATA"), "failed to validate value from cache, will reload, errors={}", errors);
                return null;
            }

            return result;
        } catch (IOException e) {
            logger.warn(errorCode("INVALID_CACHE_DATA"), "failed to deserialize value from cache, will reload, error={}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public <T> void put(String key, T value, Duration expiration, JSONMapper<T> mapper) {
        try {
            redis.set(key, mapper.toJSON(value), expiration, false);
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
        }
    }

    @Override
    public <T> void putAll(List<Entry<T>> values, Duration expiration, JSONMapper<T> mapper) {
        Map<String, byte[]> cacheValues = Maps.newHashMapWithExpectedSize(values.size());
        for (Entry<T> value : values) {
            cacheValues.put(value.key, mapper.toJSON(value.value));
        }
        try {
            redis.multiSet(cacheValues, expiration);
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String... keys) {
        try {
            long deletedKeys = redis.del(keys);
            return deletedKeys > 0;
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
            return false;
        }
    }
}
