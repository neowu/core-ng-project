package core.framework.impl.redis;

import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisSet;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
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
    public boolean add(String key, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(SADD, encode(key), encode(value));
            return connection.readLong() == 1;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, 1);
            logger.debug("sadd, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Set<String> members(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        int returnedMembers = 0;
        try {
            RedisConnection connection = item.resource;
            connection.write(SMEMBERS, encode(key));
            Object[] response = connection.readArray();
            returnedMembers = response.length;
            Set<String> members = new HashSet<>(returnedMembers);
            for (Object member : response) {
                members.add(decode((byte[]) member));
            }
            return members;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, returnedMembers, 0);
            logger.debug("smembers, key={}, elapsedTime={}", key, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public boolean isMember(String key, String value) {
        StopWatch watch = new StopWatch();
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
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 1, 0);
            logger.debug("sismember, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public boolean remove(String key, String... values) {
        StopWatch watch = new StopWatch();
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
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, values.length);
            logger.debug("srem, key={}, values={}, elapsedTime={}", key, values, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }
}
