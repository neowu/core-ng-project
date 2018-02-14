package core.framework.impl.redis;

import core.framework.impl.log.LogParam;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.redis.Redis;
import core.framework.redis.RedisHash;
import core.framework.redis.RedisSet;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import static core.framework.impl.redis.Protocol.Keyword.COUNT;
import static core.framework.impl.redis.Protocol.Keyword.EX;
import static core.framework.impl.redis.Protocol.Keyword.MATCH;
import static core.framework.impl.redis.Protocol.Keyword.NX;
import static core.framework.impl.redis.RedisEncodings.decode;
import static core.framework.impl.redis.RedisEncodings.encode;

/**
 * @author neo
 */
public final class RedisImpl implements Redis {
    private final Logger logger = LoggerFactory.getLogger(RedisImpl.class);
    private final RedisSet redisSet = new RedisSetImpl(this);
    private final RedisHash redisHash = new RedisHashImpl(this);
    private final String name;
    public Pool<RedisConnection> pool;
    public String host;
    long slowOperationThresholdInNanos = Duration.ofMillis(500).toNanos();
    Duration timeout;

    public RedisImpl(String name) {
        this.name = name;
        pool = new Pool<>(this::createConnection, name);
        pool.size(5, 50);
        pool.maxIdleTime = Duration.ofMinutes(30);
        timeout(Duration.ofSeconds(5));
    }

    public void timeout(Duration timeout) {
        this.timeout = timeout;
        pool.checkoutTimeout(timeout);
    }

    public void slowOperationThreshold(Duration threshold) {
        slowOperationThresholdInNanos = threshold.toNanos();
    }

    private RedisConnection createConnection() {
        if (host == null) throw new Error("redis.host must not be null");
        try {
            RedisConnection connection = new RedisConnection(host, timeout);
            connection.connect();
            return connection;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void close() {
        logger.info("close redis client, name={}, host={}", name, host);
        pool.close();
    }

    @Override
    public String get(String key) {
        return decode(getBytes(key));
    }

    public byte[] getBytes(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(Protocol.Command.GET, encode(key));
            return connection.readBulkString();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 1, 0);
            logger.debug("get, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void set(String key, String value) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(Protocol.Command.SET, encode(key), encode(value));
            connection.readSimpleString();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, 1);
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
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(Protocol.Command.SETEX, encode(key), encode(expiration.getSeconds()), value);
            connection.readSimpleString();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, 1);
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
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(Protocol.Command.SET, encode(key), encode(value), NX, EX, encode(expiration.getSeconds()));
            String result = connection.readSimpleString();
            return "OK".equals(result);
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, 1);
            logger.debug("setIfAbsent, key={}, value={}, expiration={}, elapsedTime={}", key, value, expiration, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void expire(String key, Duration expiration) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(Protocol.Command.EXPIRE, encode(key), encode(expiration.getSeconds()));
            connection.readLong();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, 1);
            logger.debug("expire, key={}, expiration={}, elapsedTime={}", key, expiration, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public boolean del(String key) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(Protocol.Command.DEL, encode(key));
            return connection.readLong() == 1;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, 1);
            logger.debug("del, key={}, elapsedTime={}", key, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public long increaseBy(String key, long increment) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(Protocol.Command.INCRBY, encode(key), encode(increment));
            return connection.readLong();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, 1);
            logger.debug("increaseBy, key={}, increment={}, elapsedTime={}", key, increment, elapsedTime);
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
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            byte[][] arguments = new byte[keys.length][];
            for (int i = 0; i < keys.length; i++) {
                arguments[i] = encode(keys[i]);
            }
            Map<String, byte[]> values = Maps.newHashMapWithExpectedSize(keys.length);
            connection.write(Protocol.Command.MGET, arguments);
            Object[] response = connection.readArray();
            for (int i = 0; i < response.length; i++) {
                byte[] value = (byte[]) response[i];
                if (value != null) values.put(keys[i], value);
            }
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, keys.length, 0);
            logger.debug("mget, keys={}, elapsedTime={}", keys, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    @Override
    public void multiSet(Map<String, String> values) {
        StopWatch watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.write(Protocol.Command.MSET, encode(values));
            connection.readSimpleString();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, values.size());
            logger.debug("mset, values={}, elapsedTime={}", values, elapsedTime);
            checkSlowOperation(elapsedTime);
        }
    }

    public void multiSet(Map<String, byte[]> values, Duration expiration) {
        StopWatch watch = new StopWatch();
        byte[] expirationValue = encode(expiration.getSeconds());
        PoolItem<RedisConnection> item = pool.borrowItem();
        int size = values.size();
        try {
            RedisConnection connection = item.resource;
            for (Map.Entry<String, byte[]> entry : values.entrySet()) {
                connection.write(Protocol.Command.SETEX, encode(entry.getKey()), expirationValue, entry.getValue());
            }
            connection.readAll(size);
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, 0, size);
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
        PoolItem<RedisConnection> item = pool.borrowItem();
        int count = 0;
        try {
            RedisConnection connection = item.resource;
            byte[] batchSize = encode("500"); // use 500 as batch
            String cursor = "0";
            do {
                connection.write(Protocol.Command.SCAN, encode(cursor), MATCH, encode(pattern), COUNT, batchSize);
                Object[] response = connection.readArray();
                cursor = decode((byte[]) response[0]);
                Object[] keys = (Object[]) response[1];
                for (Object key : keys) {
                    consumer.accept(decode((byte[]) key));
                }
                count += keys.length;
            } while (!"0".equals(cursor));
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("redis", elapsedTime, count, 0);
            logger.debug("forEach, pattern={}, count={}, elapsedTime={}", pattern, count, elapsedTime);
        }
    }

    void checkSlowOperation(long elapsedTime) {
        if (elapsedTime > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_REDIS"), "slow redis operation, elapsedTime={}", elapsedTime);
        }
    }
}
