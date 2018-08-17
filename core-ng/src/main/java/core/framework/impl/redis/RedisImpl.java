package core.framework.impl.redis;

import core.framework.impl.log.filter.BytesParam;
import core.framework.impl.resource.Pool;
import core.framework.impl.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.log.Markers;
import core.framework.redis.Redis;
import core.framework.redis.RedisHash;
import core.framework.redis.RedisList;
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
    private final RedisList redisList = new RedisListImpl(this);
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
        var watch = new StopWatch();
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
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 1, 0);
            logger.debug("get, key={}, elapsed={}", key, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void set(String key, String value) {
        var watch = new StopWatch();
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
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, 1);
            logger.debug("set, key={}, value={}, elapsed={}", key, value, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void set(String key, String value, Duration expiration) {
        set(key, encode(value), expiration);
    }

    public void set(String key, byte[] value, Duration expiration) {
        var watch = new StopWatch();
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
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, 1);
            logger.debug("set, key={}, value={}, expiration={}, elapsed={}", key, new BytesParam(value), expiration, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public RedisSet set() {
        return redisSet;
    }

    @Override
    public boolean setIfAbsent(String key, String value, Duration expiration) {
        var watch = new StopWatch();
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
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, 1);
            logger.debug("setIfAbsent, key={}, value={}, expiration={}, elapsed={}", key, value, expiration, elapsed);
            checkSlowOperation(elapsed);
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
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, 1);
            logger.debug("expire, key={}, expiration={}, elapsed={}", key, expiration, elapsed);
            checkSlowOperation(elapsed);
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
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, 1);
            logger.debug("del, key={}, elapsed={}", key, elapsed);
            checkSlowOperation(elapsed);
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
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, 1);
            logger.debug("increaseBy, key={}, increment={}, elapsed={}", key, increment, elapsed);
            checkSlowOperation(elapsed);
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
        var watch = new StopWatch();
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
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, keys.length, 0);
            logger.debug("mget, keys={}, size={}, elapsed={}", keys, keys.length, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void multiSet(Map<String, String> values) {
        var watch = new StopWatch();
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
            long elapsed = watch.elapsed();
            int size = values.size();
            ActionLogContext.track("redis", elapsed, 0, size);
            logger.debug("mset, values={}, size={}, elapsed={}", values, size, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    public void multiSet(Map<String, byte[]> values, Duration expiration) {
        var watch = new StopWatch();
        byte[] expirationValue = encode(expiration.getSeconds());
        PoolItem<RedisConnection> item = pool.borrowItem();
        int size = values.size();
        try {
            RedisConnection connection = item.resource;
            for (Map.Entry<String, byte[]> entry : values.entrySet()) { // redis doesn't support mset with expiration, here to use pipeline
                connection.write(Protocol.Command.SETEX, encode(entry.getKey()), expirationValue, entry.getValue());
            }
            connection.readAll(size);
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, size);
            logger.debug("mset, values={}, size={}, expiration={}, elapsed={}", values, size, expiration, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public RedisHash hash() {
        return redisHash;
    }

    @Override
    public void forEach(String pattern, Consumer<String> consumer) {
        var watch = new StopWatch();
        long start = System.nanoTime();
        long redisTook = 0;
        PoolItem<RedisConnection> item = pool.borrowItem();
        int returnedKeys = 0;
        try {
            RedisConnection connection = item.resource;
            byte[] batchSize = encode("500"); // use 500 as batch
            String cursor = "0";
            do {
                connection.write(Protocol.Command.SCAN, encode(cursor), MATCH, encode(pattern), COUNT, batchSize);
                Object[] response = connection.readArray();
                cursor = decode((byte[]) response[0]);
                Object[] keys = (Object[]) response[1];
                returnedKeys += keys.length;
                redisTook += System.nanoTime() - start;
                for (Object key : keys) {
                    consumer.accept(decode((byte[]) key));
                }
                start = System.nanoTime();
            } while (!"0".equals(cursor));
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", redisTook, returnedKeys, 0);
            logger.debug("forEach, pattern={}, returnedKeys={}, redisTook={}, elapsed={}", pattern, returnedKeys, redisTook, elapsed);
        }
    }

    @Override
    public RedisList list() {
        return redisList;
    }

    void checkSlowOperation(long elapsed) {
        if (elapsed > slowOperationThresholdInNanos) {
            logger.warn(Markers.errorCode("SLOW_REDIS"), "slow redis operation, elapsed={}", elapsed);
        }
    }
}
