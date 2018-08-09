package core.framework.impl.redis;

import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisList;
import core.framework.util.Lists;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
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
        return decode(getBytes(key));
    }

    private byte[] getBytes(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(LPOP, encode(key));
            return connection.readBulkString();
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
    public void push(String key, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(RPUSH, encode(key), encode(value));
            connection.readLong();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, 1);
            logger.debug("rpush, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public List<String> getAll(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        int returnedFields = 0;
        try {
            RedisConnection connection = item.resource;
            connection.write(LRANGE, encode(key), encode(0), encode(-1));
            Object[] response = connection.readArray();
            List<String> list = Lists.newArrayList();
            for (Object value : response) {
                list.add(decode((byte[]) value));
            }
            return list;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, returnedFields, 0);
            logger.debug("lrange, key={}, elapsedTime={}", key, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }
}
