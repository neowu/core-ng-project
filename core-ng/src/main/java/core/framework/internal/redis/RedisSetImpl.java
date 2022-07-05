package core.framework.internal.redis;

import core.framework.internal.log.filter.ArrayLogParam;
import core.framework.internal.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisSet;
import core.framework.util.Sets;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import static core.framework.internal.redis.Protocol.Command.SADD;
import static core.framework.internal.redis.Protocol.Command.SCARD;
import static core.framework.internal.redis.Protocol.Command.SISMEMBER;
import static core.framework.internal.redis.Protocol.Command.SMEMBERS;
import static core.framework.internal.redis.Protocol.Command.SPOP;
import static core.framework.internal.redis.Protocol.Command.SREM;
import static core.framework.internal.redis.RedisEncodings.decode;
import static core.framework.internal.redis.RedisEncodings.encode;
import static core.framework.internal.redis.RedisEncodings.validate;

/**
 * @author neo
 */
public final class RedisSetImpl implements RedisSet {
    private final Logger logger = LoggerFactory.getLogger(RedisSetImpl.class);
    private final RedisImpl redis;

    RedisSetImpl(RedisImpl redis) {
        this.redis = redis;
    }

    @Override
    public long add(String key, String... values) {
        var watch = new StopWatch();
        validate("key", key);
        validate("values", values);
        long addedValues = 0;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentsCommand(SADD, key, values);
            addedValues = connection.readLong();
            return addedValues;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("sadd, key={}, values={}, size={}, elapsed={}", key, new ArrayLogParam(values), values.length, elapsed);
            ActionLogContext.track("redis", elapsed, 0, (int) addedValues);
        }
    }

    @Override
    public Set<String> members(String key) {
        var watch = new StopWatch();
        validate("key", key);
        Set<String> values = null;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyCommand(SMEMBERS, key);
            Object[] response = connection.readArray();
            values = Sets.newHashSetWithExpectedSize(response.length);
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
            logger.debug("smembers, key={}, returnedValues={}, elapsed={}", key, values, elapsed);
            int readEntries = values == null ? 0 : values.size();
            ActionLogContext.track("redis", elapsed, readEntries, 0);
        }
    }

    @Override
    public boolean isMember(String key, String value) {
        var watch = new StopWatch();
        validate("key", key);
        validate("value", value);
        boolean isMember = false;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentCommand(SISMEMBER, key, encode(value));
            long response = connection.readLong();
            isMember = response == 1;
            return isMember;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("sismember, key={}, value={}, isMember={}, elapsed={}", key, value, isMember, elapsed);
            ActionLogContext.track("redis", elapsed, 1, 0);
        }
    }

    @Override
    public long remove(String key, String... values) {
        var watch = new StopWatch();
        validate("key", key);
        validate("values", values);
        long removedValues = 0;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentsCommand(SREM, key, values);
            removedValues = connection.readLong();
            return removedValues;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            int size = values.length;
            logger.debug("srem, key={}, values={}, size={}, removedValues={}, elapsed={}", key, new ArrayLogParam(values), size, removedValues, elapsed);
            ActionLogContext.track("redis", elapsed, 0, size);
        }
    }

    @Override
    public Set<String> pop(String key, long count) {
        var watch = new StopWatch();
        validate("key", key);
        if (count <= 0) throw new Error("count must be greater than 0");
        Set<String> values = null;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentCommand(SPOP, key, encode(count));
            Object[] response = connection.readArray();
            values = Sets.newHashSetWithExpectedSize(response.length);
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
            logger.debug("spop, key={}, count={}, returnedValues={}, elapsed={}", key, count, values, elapsed);
            int readEntries = values == null ? 0 : values.size();
            ActionLogContext.track("redis", elapsed, readEntries, 0);
        }
    }

    @Override
    public long size(String key) {
        var watch = new StopWatch();
        validate("key", key);
        long size = 0;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyCommand(SCARD, key);
            size = connection.readLong();
            return size;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("scard, key={}, size={}, elapsed={}", key, size, elapsed);
            ActionLogContext.track("redis", elapsed, 1, 0);
        }
    }
}
