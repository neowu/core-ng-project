package core.framework.impl.redis;

import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisSet;
import core.framework.util.Sets;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import static core.framework.impl.redis.Protocol.Command.SADD;
import static core.framework.impl.redis.Protocol.Command.SISMEMBER;
import static core.framework.impl.redis.Protocol.Command.SMEMBERS;
import static core.framework.impl.redis.Protocol.Command.SREM;
import static core.framework.impl.redis.RedisEncodings.decode;
import static core.framework.impl.redis.RedisEncodings.encode;

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
    public boolean add(String key, String... values) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(SADD, encode(key, values));
            return connection.readLong() >= 1;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            int size = values.length;
            ActionLogContext.track("redis", elapsed, 0, size);
            logger.debug("sadd, key={}, values={}, size={}, elapsed={}", key, values, size, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public Set<String> members(String key) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        int returnedValues = 0;
        try {
            RedisConnection connection = item.resource;
            connection.write(SMEMBERS, encode(key));
            Object[] response = connection.readArray();
            returnedValues = response.length;
            Set<String> values = Sets.newHashSetWithExpectedSize(returnedValues);
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
            ActionLogContext.track("redis", elapsed, returnedValues, 0);
            logger.debug("smembers, key={}, returnedValues={}, elapsed={}", key, returnedValues, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public boolean isMember(String key, String value) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(SISMEMBER, encode(key), encode(value));
            Long response = connection.readLong();
            return response == 1;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 1, 0);
            logger.debug("sismember, key={}, value={}, elapsed={}", key, value, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public boolean remove(String key, String... values) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(SREM, encode(key, values));
            return connection.readLong() >= 1;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            int size = values.length;
            ActionLogContext.track("redis", elapsed, 0, size);
            logger.debug("srem, key={}, values={}, size={}, elapsed={}", key, values, size, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }
}
