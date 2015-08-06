package core.framework.api.redis;

import core.framework.api.log.ActionLogContext;
import core.framework.api.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author neo
 */
public final class Redis {
    private final Logger logger = LoggerFactory.getLogger(Redis.class);
    private final JedisPool redisPool;
    private final long slowQueryThresholdInMs;

    Redis(JedisPool redisPool, long slowQueryThresholdInMs) {
        this.redisPool = redisPool;
        this.slowQueryThresholdInMs = slowQueryThresholdInMs;
    }

    public void shutdown() {
        logger.info("shutdown redis connection pool");
        redisPool.destroy();
    }

    public String get(String key) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            return redis.get(key);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("get, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void set(String key, String value) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            redis.set(key, value);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("set, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void expire(String key, Duration duration) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            redis.expire(key, (int) duration.getSeconds());
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("expire, key={}, duration={}, elapsedTime={}", key, duration, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void setExpire(String key, String value, Duration duration) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            redis.setex(key, (int) duration.getSeconds(), value);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("setExpire, key={}, value={}, duration={}, elapsedTime={}", key, value, duration, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void del(String... keys) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            redis.del(keys);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("del, keys={}, elapsedTime={}", keys, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public Map<String, String> hgetAll(String key) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            return redis.hgetAll(key);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hgetAll, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void hmset(String key, Map<String, String> value) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            redis.hmset(key, value);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hmset, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public Set<String> keys(String pattern) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            return redis.keys(pattern);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("keys, pattern={}, elapsedTime={}", pattern, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void lpush(String key, String value) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            redis.lpush(key, value);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("lpush, key={}, value={}, elapsedTime={}", key, value, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    // blocking right pop
    public String brpop(String key) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            List<String> result = redis.brpop(key, "0");
            return result.get(1);   // result[0] is key, result[1] is popped value
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("brpop, key={}, elapsedTime={}", key, elapsedTime);
        }
    }

    public void pipeline(Consumer<Pipeline> consumer) {
        StopWatch watch = new StopWatch();
        try (Jedis redis = redisPool.getResource()) {
            Pipeline pipeline = redis.pipelined();
            consumer.accept(pipeline);
            pipeline.sync();
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("pipeline, elapsedTime={}", elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    private void checkSlowQuery(long elapsedTime) {
        if (elapsedTime > slowQueryThresholdInMs) {
            int activeNum = redisPool.getNumActive();
            int idleNum = redisPool.getNumIdle();
            int waitingNum = redisPool.getNumWaiters();
            logger.debug("redis pool stats, active={}, idle={}, waiting={}", activeNum, idleNum, waitingNum);
            logger.warn("slow query detected");
        }
    }
}
