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
    public List<String> pop(String key, int count) {
        var watch = new StopWatch();
        validate("key", key);
        if (count <= 0) throw new Error("count must be greater than 0");
        List<String> values = new ArrayList<>(count);
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            if (count == 1) {   // "lpop key count" only be supported since redis 6.2, to use old protocol if count=1
                connection.writeKeyCommand(LPOP, key);
                values.add(decode(connection.readBlobString()));
            } else {
                connection.writeKeyArgumentCommand(LPOP, key, encode(count));
                Object[] response = connection.readArray();
                if (response != null) {     // lpop returns nil array if no element, this is different behavior of other pop (e.g. spop), it's likely due to blpop impl, use nil array to distinguish between timeout and empty list
                    for (Object value : response) {
                        values.add(decode((byte[]) value));
                    }
                }
            }
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, values.size(), 0);
            logger.debug("lpop, key={}, count={}, returnedValues={}, elapsed={}", key, count, values, elapsed);
            redis.checkSlowOperation(elapsed);
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
            ActionLogContext.track("redis", elapsed, 0, values.length);
            logger.debug("rpush, key={}, values={}, size={}, elapsed={}", key, new ArrayLogParam(values), values.length, elapsed);
            redis.checkSlowOperation(elapsed);
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
            ActionLogContext.track("redis", elapsed, values == null ? 0 : values.size(), 0);
            logger.debug("lrange, key={}, start={}, stop={}, returnedValues={}, elapsed={}", key, start, stop, values, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }
}
