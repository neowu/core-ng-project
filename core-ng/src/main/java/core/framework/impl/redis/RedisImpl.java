package core.framework.impl.redis;

import core.framework.impl.log.LogParam;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.redis.Redis;
import core.framework.redis.RedisHash;
import core.framework.redis.RedisSet;
import core.framework.util.Charsets;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author neo
 */
public final class RedisImpl implements Redis {
    private static final byte[] NX = Strings.bytes("NX");
    private static final byte[] EX = Strings.bytes("EX");
    public final Pool<BinaryJedis> pool;
    private final Logger logger = LoggerFactory.getLogger(RedisImpl.class);
    private final RedisSet redisSet = new RedisSetImpl(this);
    private final RedisHash redisHash = new RedisHashImpl(this);
    private String host;
    private long slowOperationThresholdInNanos = Duration.ofMillis(500).toNanos();
    private Duration timeout;

    public RedisImpl() {
        pool = new Pool<>(this::createClient, BinaryJedis::close);
        pool.name("redis");
        pool.size(5, 50);
        pool.maxIdleTime(Duration.ofMinutes(30));
        timeout(Duration.ofSeconds(5));
    }

    public void host(String host) {
        this.host = host;
    }

    public void timeout(Duration timeout) {
        this.timeout = timeout;
        pool.checkoutTimeout(timeout);
    }

    public void slowOperationThreshold(Duration slowOperationThreshold) {
        slowOperationThresholdInNanos = slowOperationThreshold.toNanos();
    }

    private BinaryJedis createClient() {
        if (host == null) throw new Error("redis.host must not be null");
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
    public RedisSet set() {
        return redisSet;
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
    public Map<String, String> multiGet(String... keys) {
        Map<String, byte[]> values = multiGetBytes(keys);
        Map<String, String> result = Maps.newHashMapWithExpectedSize(values.size());
        for (Map.Entry<String, byte[]> entry : values.entrySet()) {
            result.put(entry.getKey(), decode(entry.getValue()));
        }
        return result;
    }

    public Map<String, byte[]> multiGetBytes(String... keys) {
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
    public void multiSet(Map<String, String> values) {
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

    public void multiSet(Map<String, byte[]> values, Duration expiration) {
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
    public RedisHash hash() {
        return redisHash;
    }

    @Override
    public void forEach(String pattern, Consumer<String> consumer) {
        StopWatch watch = new StopWatch();
        PoolItem<BinaryJedis> item = pool.borrowItem();
        int count = 0;
        try {
            ScanParams params = new ScanParams().match(pattern).count(500); // use 500 as batch
            String cursor = "0";
            do {
                ScanResult<byte[]> result = item.resource.scan(encode(cursor), params);
                cursor = decode(result.getCursorAsBytes());
                List<byte[]> keys = result.getResult();
                for (byte[] key : keys) {
                    count++;
                    consumer.accept(decode(key));
                }
            } while (!"0".equals(cursor));
        } catch (JedisConnectionException e) {
            item.broken = true;
            throw e;
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime);
            logger.debug("forEach, pattern={}, count={}, elapsedTime={}", pattern, count, elapsedTime);
        }
    }

    byte[] encode(String value) {   // redis does not accept null
        return Strings.bytes(value);
    }

    byte[][] encode(String[] values) {
        int size = values.length;
        byte[][] redisValues = new byte[size][];
        for (int i = 0; i < size; i++) {
            redisValues[i] = encode(values[i]);
        }
        return redisValues;
    }

    String decode(byte[] value) {
        if (value == null) return null;
        return new String(value, Charsets.UTF_8);
    }

    void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_REDIS"), "slow redis operation, elapsedTime={}", elapsedTime);
        }
    }
}
