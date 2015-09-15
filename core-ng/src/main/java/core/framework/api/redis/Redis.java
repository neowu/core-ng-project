package core.framework.api.redis;

import core.framework.api.log.ActionLogContext;
import core.framework.api.util.Charsets;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Strings;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public final class Redis {
    private static final byte[] NX = Strings.bytes("NX");
    private static final byte[] EX = Strings.bytes("EX");

    private final Logger logger = LoggerFactory.getLogger(Redis.class);
    private final String host;

    public final Pool<BinaryJedis> pool;
    private long slowQueryThresholdInMs = 200;
    private Duration timeout;

    public Redis(String host) {
        this.host = host;
        pool = new Pool<>(this::createClient, BinaryJedis::close);
        pool.name("redis");
        pool.size(5, 50);
        pool.maxIdleTime(Duration.ofMinutes(30));
        timeout(Duration.ofSeconds(2));
    }

    public void timeout(Duration timeout) {
        this.timeout = timeout;
        pool.checkoutTimeout(timeout);
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        slowQueryThresholdInMs = slowQueryThreshold.toMillis();
    }

    private BinaryJedis createClient() {
        BinaryJedis client = new BinaryJedis(host, Protocol.DEFAULT_PORT, (int) timeout.toMillis());
        client.connect();
        return client;
    }

    public void close() {
        logger.info("close redis client, host={}", host);
        pool.close();
    }

    public String get(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            return decode(item.resource.get(encode(key)));
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
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            item.resource.set(encode(key), encode(value));
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

    public void set(String key, String value, Duration expiration) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            item.resource.setex(encode(key), (int) expiration.getSeconds(), encode(value));
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("setExpire, key={}, value={}, expiration={}, elapsedTime={}", key, value, expiration, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public boolean setIfAbsent(String key, String value, Duration expiration) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            String result = item.resource.set(encode(key), encode(value), NX, EX, expiration.getSeconds());
            return "OK".equals(result);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("setIfAbsent, key={}, value={}, expiration={}, elapsedTime={}", key, value, expiration, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public void expire(String key, Duration duration) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            item.resource.expire(encode(key), (int) duration.getSeconds());
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

    public void del(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            item.resource.del(encode(key));
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("del, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    public Map<String, String> hgetAll(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            Map<byte[], byte[]> binaryResults = item.resource.hgetAll(encode(key));
            Map<String, String> results = Maps.newHashMapWithExpectedSize(binaryResults.size());
            for (Map.Entry<byte[], byte[]> entry : binaryResults.entrySet()) {
                results.put(decode(entry.getKey()), decode(entry.getValue()));
            }
            return results;
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
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            Map<byte[], byte[]> binaryValue = Maps.newHashMapWithExpectedSize(value.size());
            for (Map.Entry<String, String> entry : value.entrySet()) {
                binaryValue.put(encode(entry.getKey()), encode(entry.getValue()));
            }
            item.resource.hmset(encode(key), binaryValue);
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

    public void lpush(String key, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            item.resource.lpush(encode(key), encode(value));
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
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            List<byte[]> result = item.resource.brpop(encode(key), encode("0"));
            return decode(result.get(1));   // result[0] is key, result[1] is popped value
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

    private byte[] encode(String value) {
        return Strings.bytes(value);
    }

    private String decode(byte[] value) {
        if (value == null) return null;
        return new String(value, Charsets.UTF_8);
    }

    private void checkSlowQuery(long elapsedTime) {
        if (elapsedTime > slowQueryThresholdInMs) {
            logger.warn("slow query detected");
        }
    }
}
