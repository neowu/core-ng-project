package core.framework.impl.redis;

import core.framework.api.log.ActionLogContext;
import core.framework.api.log.Markers;
import core.framework.api.redis.Redis;
import core.framework.api.util.Charsets;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.api.util.Strings;
import core.framework.impl.log.LogParam;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
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
    private long slowOperationThresholdInMs = 500;
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

    public void slowOperationThreshold(Duration slowOperationThreshold) {
        slowOperationThresholdInMs = slowOperationThreshold.toMillis();
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
        return decode(getBytes(key));
    }

    public byte[] getBytes(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            return item.resource.get(encode(key));
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("get, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowOperation(elapsedTime);
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
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void set(String key, String value, Duration expiration) {
        set(key, encode(value), expiration);
    }

    public void set(String key, byte[] value, Duration expiration) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            item.resource.setex(encode(key), (int) expiration.getSeconds(), value);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("set, key={}, value={}, expiration={}, elapsedTime={}", key, LogParam.of(value), expiration, elapsedTime);
            checkSlowOperation(elapsedTime);
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
            checkSlowOperation(elapsedTime);
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
            checkSlowOperation(elapsedTime);
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
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Map<String, String> mget(String... keys) {
        Map<String, byte[]> values = mgetBytes(keys);
        Map<String, String> result = Maps.newHashMapWithExpectedSize(values.size());
        for (Map.Entry<String, byte[]> entry : values.entrySet()) {
            result.put(entry.getKey(), decode(entry.getValue()));
        }
        return result;
    }

    public Map<String, byte[]> mgetBytes(String... keys) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            byte[][] redisKeys = encode(keys);
            Map<String, byte[]> values = Maps.newHashMapWithExpectedSize(keys.length);
            List<byte[]> redisValues = item.resource.mget(redisKeys);
            int index = 0;
            for (byte[] redisValue : redisValues) {
                if (redisValue != null) values.put(keys[index], redisValue);
                index++;
            }
            return values;
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("mget, keys={}, elapsedTime={}", keys, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void mset(Map<String, String> values) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            byte[][] keyValues = new byte[values.size() * 2][];
            int i = 0;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                keyValues[i] = encode(key);
                keyValues[i + 1] = encode(value);
                i = i + 2;
            }
            item.resource.mset(keyValues);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("mset, values={}, elapsedTime={}", values, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    public void mset(Map<String, byte[]> values, Duration expiration) {
        StopWatch watch = new StopWatch();
        int expirationInSeconds = (int) expiration.getSeconds();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try (Pipeline pipeline = item.resource.pipelined()) {
            for (Map.Entry<String, byte[]> entry : values.entrySet()) {
                pipeline.setex(encode(entry.getKey()), expirationInSeconds, entry.getValue());
            }
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("mset, values={}, expiration={}, elapsedTime={}", LogParam.of(values), expiration, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            Map<byte[], byte[]> redisValues = item.resource.hgetAll(encode(key));
            Map<String, String> values = Maps.newHashMapWithExpectedSize(redisValues.size());
            for (Map.Entry<byte[], byte[]> entry : redisValues.entrySet()) {
                values.put(decode(entry.getKey()), decode(entry.getValue()));
            }
            return values;
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hgetAll, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void hmset(String key, Map<String, String> values) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        try {
            Map<byte[], byte[]> redisValues = Maps.newHashMapWithExpectedSize(values.size());
            for (Map.Entry<String, String> entry : values.entrySet()) {
                redisValues.put(encode(entry.getKey()), encode(entry.getValue()));
            }
            item.resource.hmset(encode(key), redisValues);
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("hmset, key={}, values={}, elapsedTime={}", key, values, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    private byte[] encode(String value) {
        return Strings.bytes(value);
    }

    private byte[][] encode(String[] keys) {
        int size = keys.length;
        byte[][] redisKeys = new byte[size][];
        for (int i = 0; i < size; i++) {
            redisKeys[i] = encode(keys[i]);
        }
        return redisKeys;
    }

    private String decode(byte[] value) {
        if (value == null) return null;
        return new String(value, Charsets.UTF_8);
    }

    private void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > slowOperationThresholdInMs) {
            logger.warn(Markers.errorCode("SLOW_REDIS"), "slow redis operation, elapsedTime={}", elapsedTime);
        }
    }
}
