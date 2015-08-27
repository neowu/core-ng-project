package core.framework.api.redis;

import core.framework.api.log.ActionLogContext;
import core.framework.api.util.StopWatch;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public final class Redis {
    private final Logger logger = LoggerFactory.getLogger(Redis.class);
    private final String host;

    public final Pool<Jedis> pool;
    private long slowQueryThresholdInMs = 100;
    public Duration timeout = Duration.ofSeconds(2);

    public Redis(String host) {
        this.host = host;
        pool = new Pool<>(this::createRedis, Jedis::close);
        pool.name("redis");
        pool.configure(5, 50, Duration.ofMinutes(30));
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        slowQueryThresholdInMs = slowQueryThreshold.toMillis();
    }

    private Jedis createRedis() {
        Jedis jedis = new Jedis(host, Protocol.DEFAULT_PORT, (int) timeout.toMillis());
        jedis.connect();
        return jedis;
    }

    public void close() {
        logger.info("close redis client, host={}", host);
        pool.close();
    }

    public String get(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            return item.resource.get(key);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("get, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void set(String key, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            item.resource.set(key, value);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("set, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void expire(String key, Duration duration) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            item.resource.expire(key, (int) duration.getSeconds());
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("expire, key={}, duration={}, elapsedTime={}", key, duration, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void setExpire(String key, String value, Duration duration) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            item.resource.setex(key, (int) duration.getSeconds(), value);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("setExpire, key={}, value={}, duration={}, elapsedTime={}", key, value, duration, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void del(String... keys) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            item.resource.del(keys);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("del, keys={}, elapsedTime={}", keys, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public Map<String, String> hgetAll(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            return item.resource.hgetAll(key);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hgetAll, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void hmset(String key, Map<String, String> value) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            item.resource.hmset(key, value);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hmset, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public Set<String> keys(String pattern) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            return item.resource.keys(pattern);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("keys, pattern={}, elapsedTime={}", pattern, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void lpush(String key, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            item.resource.lpush(key, value);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("lpush, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    // blocking right pop
    public String brpop(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<Jedis> item = pool.borrowItem();
        try {
            List<String> result = item.resource.brpop(key, "0");
            return result.get(1);   // result[0] is key, result[1] is popped value
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("brpop, key={}, elapsedTime={}", key, elapsedTime);
        }
    }

    private void checkSlowQuery(long elapsedTime) {
        if (elapsedTime > slowQueryThresholdInMs) {
            logger.warn("slow query detected");
        }
    }
}
