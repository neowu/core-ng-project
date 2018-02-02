package core.framework.impl.redis;

import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisHash;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import static core.framework.impl.redis.Protocol.Command.HDEL;
import static core.framework.impl.redis.Protocol.Command.HGET;
import static core.framework.impl.redis.Protocol.Command.HGETALL;
import static core.framework.impl.redis.Protocol.Command.HMSET;
import static core.framework.impl.redis.Protocol.Command.HSET;
import static core.framework.impl.redis.RedisEncodings.decode;
import static core.framework.impl.redis.RedisEncodings.encode;

/**
 * @author neo
 */
public final class RedisHashImpl implements RedisHash {
    private final Logger logger = LoggerFactory.getLogger(RedisHashImpl.class);
    private final RedisImpl redis;

    RedisHashImpl(RedisImpl redis) {
        this.redis = redis;
    }

    @Override
    public String get(String key, String field) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(HGET, encode(key), encode(field));
            return decode(connection.readBulkString());
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 1, 0);
            logger.debug("hget, key={}, field={}, elapsedTime={}", key, field, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Map<String, String> getAll(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        int returnedFields = 0;
        try {
            RedisConnection connection = item.resource;
            connection.write(HGETALL, encode(key));
            Object[] response = connection.readArray();
            if (response.length % 2 != 0) throw new RedisException("unexpected length of array, length=" + response.length);
            returnedFields = response.length / 2;
            Map<String, String> values = Maps.newHashMapWithExpectedSize(returnedFields);
            for (int i = 0; i < response.length; i += 2) {
                values.put(decode((byte[]) response[i]), decode((byte[]) response[i + 1]));
            }
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, returnedFields, 0);
            logger.debug("hgetAll, key={}, elapsedTime={}", key, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void set(String key, String field, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(HSET, encode(key), encode(field), encode(value));
            connection.readLong();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, 1);
            logger.debug("hset, key={}, field={}, value={}, elapsedTime={}", key, field, value, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void multiSet(String key, Map<String, String> values) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(HMSET, encode(key, values));
            connection.readSimpleString();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, values.size());
            logger.debug("hmset, key={}, values={}, elapsedTime={}", key, values, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public boolean del(String key, String... fields) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(HDEL, encode(key, fields));
            return connection.readLong() >= 1;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, fields.length);
            logger.debug("hdel, key={}, fields={}, elapsedTime={}", key, fields, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }
}
