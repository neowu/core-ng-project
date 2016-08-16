package core.framework.impl.redis;

import core.framework.api.log.ActionLogContext;
import core.framework.api.redis.RedisHash;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.resource.PoolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;

/**
 * @author neo
 */
public final class RedisHashImpl implements RedisHash {
    private final Logger logger = LoggerFactory.getLogger(RedisHashImpl.class);
    private final RedisImpl redis;

    public RedisHashImpl(RedisImpl redis) {
        this.redis = redis;
    }

    @Override
    public String get(String key, String field) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = redis.pool.borrowItem();
        try {
            return redis.decode(item.resource.hget(redis.encode(key), redis.encode(field)));
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hget, key={}, field={}, elapsedTime={}", key, field, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Map<String, String> getAll(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = redis.pool.borrowItem();
        try {
            Map<byte[], byte[]> redisValues = item.resource.hgetAll(redis.encode(key));
            Map<String, String> values = Maps.newHashMapWithExpectedSize(redisValues.size());
            for (Map.Entry<byte[], byte[]> entry : redisValues.entrySet()) {
                values.put(redis.decode(entry.getKey()), redis.decode(entry.getValue()));
            }
            return values;
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hgetAll, key={}, elapsedTime={}", key, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void set(String key, String field, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = redis.pool.borrowItem();
        try {
            item.resource.hset(redis.encode(key), redis.encode(field), redis.encode(value));
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hset, key={}, field={}, value={}, elapsedTime={}", key, field, value, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void multiSet(String key, Map<String, String> values) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = redis.pool.borrowItem();
        try {
            Map<byte[], byte[]> redisValues = Maps.newHashMapWithExpectedSize(values.size());
            for (Map.Entry<String, String> entry : values.entrySet()) {
                redisValues.put(redis.encode(entry.getKey()), redis.encode(entry.getValue()));
            }
            item.resource.hmset(redis.encode(key), redisValues);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            redis.pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hmset, key={}, values={}, elapsedTime={}", key, values, elapsedTime);
            redis.checkSlowOperation(elapsedTime);
        }
    }
}
