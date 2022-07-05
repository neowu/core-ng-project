package core.framework.internal.redis;

import core.framework.internal.log.filter.ArrayLogParam;
import core.framework.internal.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisList;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import static core.framework.internal.redis.Protocol.Command.LPOP;
import static core.framework.internal.redis.Protocol.Command.LRANGE;
import static core.framework.internal.redis.Protocol.Command.LTRIM;
import static core.framework.internal.redis.Protocol.Command.RPUSH;
import static core.framework.internal.redis.RedisEncodings.decode;
import static core.framework.internal.redis.RedisEncodings.encode;
import static core.framework.internal.redis.RedisEncodings.validate;

/**
 * @author rexthk
 */
public final class RedisListImpl implements RedisList {
    private final Logger logger = LoggerFactory.getLogger(RedisListImpl.class);
    private final RedisImpl redis;

    RedisListImpl(RedisImpl redis) {
        this.redis = redis;
    }

    @Override
    public List<String> pop(String key, int size) {
        var watch = new StopWatch();
        validate("key", key);
        if (size <= 0) throw new Error("size must be greater than 0");
        List<String> values = new ArrayList<>(size);
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentCommand(LPOP, key, encode(size));
            Object[] response = connection.readArray();
            if (response != null) {     // lpop returns nil array if no element, this is different behavior of other pop (e.g. spop), it's likely due to blpop impl, use nil array to distinguish between timeout and empty list
                for (Object value : response) {
                    values.add(decode((byte[]) value));
                }
            }
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("lpop, key={}, size={}, returnedValues={}, elapsed={}", key, size, values, elapsed);
            ActionLogContext.track("redis", elapsed, values.size(), 0);
        }
    }

    @Override
    public long push(String key, String... values) {
        var watch = new StopWatch();
        validate("key", key);
        validate("values", values);
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentsCommand(RPUSH, key, values);
            return connection.readLong();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("rpush, key={}, values={}, size={}, elapsed={}", key, new ArrayLogParam(values), values.length, elapsed);
            ActionLogContext.track("redis", elapsed, 0, values.length);
        }
    }

    @Override
    public List<String> range(String key, long start, long stop) {
        var watch = new StopWatch();
        validate("key", key);
        List<String> values = null;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeArray(4);
            connection.writeBlobString(LRANGE);
            connection.writeBlobString(encode(key));
            connection.writeBlobString(encode(start));
            connection.writeBlobString(encode(stop));
            connection.flush();
            Object[] response = connection.readArray();
            values = new ArrayList<>(response.length);
            for (Object value : response) {
                values.add(decode((byte[]) value));
            }
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("lrange, key={}, start={}, stop={}, returnedValues={}, elapsed={}", key, start, stop, values, elapsed);
            int readEntries = values == null ? 0 : values.size();
            ActionLogContext.track("redis", elapsed, readEntries, 0);
        }
    }

    @Override
    public void trim(String key, int maxSize) {
        var watch = new StopWatch();
        validate("key", key);
        if (maxSize <= 0) throw new Error("maxSize must be greater than 0");
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentsCommand(LTRIM, key, encode(-maxSize), encode(-1));
            connection.readSimpleString();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("ltrim, key={}, maxSize={}, elapsed={}", key, maxSize, elapsed);
            ActionLogContext.track("redis", elapsed, 0, 1);
        }
    }
}
