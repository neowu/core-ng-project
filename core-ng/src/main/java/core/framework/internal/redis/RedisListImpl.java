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
    public String pop(String key) {
        var watch = new StopWatch();
        String value = null;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyCommand(LPOP, key);
            value = decode(connection.readBlobString());
            return value;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 1, 0);
            logger.debug("lpop, key={}, returnedValue={}, elapsed={}", key, value, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public long push(String key, String... values) {
        var watch = new StopWatch();
        if (values.length == 0) throw new Error("values must not be empty");
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
    public List<String> range(String key, long start, long end) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        List<String> values = null;
        try {
            RedisConnection connection = item.resource;
            connection.writeArray(4);
            connection.writeBlobString(LRANGE);
            connection.writeBlobString(encode(key));
            connection.writeBlobString(encode(start));
            connection.writeBlobString(encode(end));
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
            logger.debug("lrange, key={}, start={}, end={}, returnedValues={}, elapsed={}", key, start, end, values, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }
}
