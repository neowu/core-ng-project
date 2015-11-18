package core.framework.impl.redis;

import core.framework.api.log.ActionLogContext;
import core.framework.api.redis.Redis;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public final class RedisImpl implements Redis {
    private static final byte[] NX = Strings.bytes("NX");
    private static final byte[] EX = Strings.bytes("EX");
    public final Pool<BinaryJedis> pool;
    private final Logger logger = LoggerFactory.getLogger(RedisImpl.class);
    private String host;
    private long slowQueryThresholdInMs = 200;
    private Duration timeout;

    public RedisImpl() {
        pool = new Pool<>(this::createClient, BinaryJedis::close);
        pool.name("redis");
        pool.size(5, 50);
        pool.maxIdleTime(Duration.ofMinutes(30));
        timeout(Duration.ofSeconds(2));
    }

    public void host(String host) {
        this.host = host;
    }

    public void timeout(Duration timeout) {
        this.timeout = timeout;
        pool.checkoutTimeout(timeout);
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        slowQueryThresholdInMs = slowQueryThreshold.toMillis();
    }

    private BinaryJedis createClient() {
        if (host == null) throw new Error("host must not be null");
        BinaryJedis client = new BinaryJedis(host, Protocol.DEFAULT_PORT, (int) timeout.toMillis());
        client.connect();
        return client;
    }

    public void close() {
        logger.info("close redis client, host={}", host);
        pool.close();
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public List<String> mget(List<String> keys) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            int size = keys.size();
            byte[][] binaryKeys = new byte[size][];
            int i = 0;
            for (String key : keys) {
                binaryKeys[i] = encode(key);
                i++;
            }
            List<String> results = new ArrayList<>(size);
            List<byte[]> binaryResults = item.resource.mget(binaryKeys);
            for (byte[] binaryResult : binaryResults) {
                results.add(decode(binaryResult));
            }
            return results;
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("mget, keys={}, elapsedTime={}", keys, elapsedTime);
            checkSlowQuery(elapsedTime);
        }
    }

    @Override
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

    @Override
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
