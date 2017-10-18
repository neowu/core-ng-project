package core.framework.impl.redis;

import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisSet;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.HashSet;
import java.util.Set;

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
        PoolItem<BinaryJedis> item = redis.pool.borrowItem();
        try {
            Long reply = item.resource.sadd(redis.encode(key), redis.encode(value));
            return reply == 1;
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("sadd, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Set<String> members(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = redis.pool.borrowItem();
        try {
            Set<byte[]> redisMembers = item.resource.smembers(redis.encode(key));
            Set<String> members = new HashSet<>(redisMembers.size());
            for (byte[] redisMember : redisMembers) {
                members.add(redis.decode(redisMember));
            }
            return members;
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("smembers, key={}, elapsedTime={}", key, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public boolean isMember(String key, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = redis.pool.borrowItem();
        try {
            return item.resource.sismember(redis.encode(key), redis.encode(value));
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("sismember, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public boolean remove(String key, String... values) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = redis.pool.borrowItem();
        try {
            Long reply = item.resource.srem(redis.encode(key), redis.encode(values));
            return reply == 1;
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("srem, key={}, values={}, elapsedTime={}", key, values, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }
}
