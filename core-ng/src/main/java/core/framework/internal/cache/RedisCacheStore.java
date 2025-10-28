package core.framework.internal.cache;

import core.framework.internal.json.JSONReader;
import core.framework.internal.redis.RedisException;
import core.framework.internal.redis.RedisImpl;
import core.framework.internal.validate.Validator;
import core.framework.log.ActionLogContext;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import org.jspecify.annotations.Nullable;
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

    @Nullable
    @Override
    public <T> T get(String key, CacheContext<T> context) {
        var watch = new StopWatch();
        long readBytes = 0;
        try {
            byte[] value = redis.getBytes(key);
            if (value == null) return null;
            readBytes = value.length;
            return deserialize(value, context.reader, context.validator);
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
            return null;
        } finally {
            ActionLogContext.track("cache", watch.elapsed(), 1, 0, readBytes, 0);
        }
    }

    @Override
    public <T> Map<String, T> getAll(String[] keys, CacheContext<T> context) {
        var watch = new StopWatch();
        int readKeys = 0;
        long readBytes = 0;
        try {
            Map<String, byte[]> redisValues = redis.multiGetBytes(keys);
            readKeys = redisValues.size();
            Map<String, T> values = Maps.newHashMapWithExpectedSize(redisValues.size());
            for (Map.Entry<String, byte[]> entry : redisValues.entrySet()) {
                byte[] bytes = entry.getValue();
                readBytes += bytes.length;

                T value = deserialize(bytes, context.reader, context.validator);
                if (value != null) {
                    values.put(entry.getKey(), value);
                }
            }
            return values;
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
            return Map.of();
        } finally {
            ActionLogContext.track("cache", watch.elapsed(), readKeys, 0, readBytes, 0);
        }
    }

    @Nullable
    private <T> T deserialize(byte[] value, JSONReader<T> reader, Validator<T> validator) {
        try {
            T result = reader.fromJSON(value);
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
    public <T> void put(String key, T value, Duration expiration, CacheContext<T> context) {
        var watch = new StopWatch();
        long writeBytes = 0;
        try {
            byte[] bytes = context.writer.toJSON(value);
            writeBytes = bytes.length;

            redis.set(key, bytes, expiration, false);
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
        } finally {
            ActionLogContext.track("cache", watch.elapsed(), 0, 1, 0, writeBytes);
        }
    }

    @Override
    public <T> void putAll(List<Entry<T>> values, Duration expiration, CacheContext<T> context) {
        var watch = new StopWatch();
        long writeBytes = 0;
        int size = values.size();
        Map<String, byte[]> cacheValues = Maps.newHashMapWithExpectedSize(size);
        for (Entry<T> value : values) {
            byte[] bytes = context.writer.toJSON(value.value());
            writeBytes += bytes.length;

            cacheValues.put(value.key(), bytes);
        }
        try {
            redis.multiSet(cacheValues, expiration);
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
        } finally {
            ActionLogContext.track("cache", watch.elapsed(), 0, size, 0, writeBytes);
        }
    }

    @Override
    public boolean delete(String... keys) {
        var watch = new StopWatch();
        long deletedKeys = 0;
        try {
            deletedKeys = redis.del(keys);
            return deletedKeys > 0;
        } catch (UncheckedIOException | RedisException e) {
            logger.warn(errorCode("CACHE_STORE_FAILED"), "failed to connect to redis, error={}", e.getMessage(), e);
            return false;
        } finally {
            ActionLogContext.track("cache", watch.elapsed(), 0, (int) deletedKeys, 0, 0);
        }
    }
}
