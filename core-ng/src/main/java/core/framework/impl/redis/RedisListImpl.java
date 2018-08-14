package core.framework.impl.redis;

import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisList;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import static core.framework.impl.redis.Protocol.Command.LPOP;
import static core.framework.impl.redis.Protocol.Command.LRANGE;
import static core.framework.impl.redis.Protocol.Command.RPUSH;
import static core.framework.impl.redis.RedisEncodings.decode;
import static core.framework.impl.redis.RedisEncodings.encode;

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
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(LPOP, encode(key));
            return decode(connection.readBulkString());
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 1, 0);
            logger.debug("lpop, key={}, elapsedTime={}", key, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public long push(String key, String... values) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(RPUSH, encode(key, values));
            return connection.readLong();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, values.length);
            logger.debug("rpush, key={}, values={}, elapsedTime={}", key, values, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public List<String> range(String key, int start, int end) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        int returnedFields = 0;
        try {
            RedisConnection connection = item.resource;
            connection.write(LRANGE, encode(key), encode(start), encode(end));
            Object[] response = connection.readArray();
            List<String> items = new ArrayList<>(response.length);
            for (Object value : response) {
                items.add(decode((byte[]) value));
            }
            return items;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, returnedFields, 0);
            logger.debug("lrange, key={}, start={}, end={}, elapsedTime={}", key, start, end, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }
}
