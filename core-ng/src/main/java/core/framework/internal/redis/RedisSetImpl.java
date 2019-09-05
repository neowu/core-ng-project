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
        if (values.length == 0) throw new Error("values must not be empty");
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
            ActionLogContext.track("redis", elapsed, 0, (int) addedValues);
            logger.debug("sadd, key={}, values={}, size={}, elapsed={}", key, new ArrayLogParam(values), values.length, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public Set<String> members(String key) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        Set<String> values = null;
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
            ActionLogContext.track("redis", elapsed, values == null ? 0 : values.size(), 0);
            logger.debug("smembers, key={}, returnedValues={}, elapsed={}", key, values, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public boolean isMember(String key, String value) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        boolean isMember = false;
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentCommand(SISMEMBER, key, encode(value));
            Long response = connection.readLong();
            isMember = response == 1;
            return isMember;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 1, 0);
            logger.debug("sismember, key={}, value={}, isMember={}, elapsed={}", key, value, isMember, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public long remove(String key, String... values) {
        var watch = new StopWatch();
        if (values.length == 0) throw new Error("values must not be empty");
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
            ActionLogContext.track("redis", elapsed, 0, size);
            logger.debug("srem, key={}, values={}, size={}, removedValues={}, elapsed={}", key, new ArrayLogParam(values), size, removedValues, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public Set<String> pop(String key, long count) {
        var watch = new StopWatch();
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
            ActionLogContext.track("redis", elapsed, values == null ? 0 : values.size(), 0);
            logger.debug("spop, key={}, count={}, returnedValues={}, elapsed={}", key, count, values, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public long size(String key) {
        var watch = new StopWatch();
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
            ActionLogContext.track("redis", elapsed, 1, 0);
            logger.debug("scard, key={}, size={}, elapsed={}", key, size, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }
}
