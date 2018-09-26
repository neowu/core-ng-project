package core.framework.impl.redis;

import core.framework.impl.log.filter.ArrayLogParam;
import core.framework.impl.log.filter.BytesLogParam;
import core.framework.impl.log.filter.MapLogParam;
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

import static core.framework.impl.redis.Protocol.Command.DEL;
import static core.framework.impl.redis.Protocol.Command.EXPIRE;
import static core.framework.impl.redis.Protocol.Command.GET;
import static core.framework.impl.redis.Protocol.Command.INCRBY;
import static core.framework.impl.redis.Protocol.Command.MGET;
import static core.framework.impl.redis.Protocol.Command.MSET;
import static core.framework.impl.redis.Protocol.Command.SCAN;
import static core.framework.impl.redis.Protocol.Command.SET;
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
    int timeoutInMs;

    public RedisImpl(String name) {
        this.name = name;
        pool = new Pool<>(this::createConnection, name);
        pool.size(5, 50);
        pool.maxIdleTime = Duration.ofMinutes(30);
        timeout(Duration.ofSeconds(5));
    }

    public void timeout(Duration timeout) {
        timeoutInMs = (int) timeout.toMillis();
        pool.checkoutTimeout(timeout);
    }

    public void slowOperationThreshold(Duration threshold) {
        slowOperationThresholdInNanos = threshold.toNanos();
    }

    private RedisConnection createConnection() {
        if (host == null) throw new Error("redis.host must not be null");
        try {
            var connection = new RedisConnection();
            connection.connect(host, timeoutInMs);
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
        byte[] value = null;
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyCommand(GET, key);
            value = connection.readBulkString();
            return value;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 1, 0);
            logger.debug("get, key={}, returnedValue={}, elapsed={}", key, new BytesLogParam(value), elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public RedisSet set() {
        return redisSet;
    }

    @Override
    public boolean set(String key, String value, Duration expiration, boolean onlyIfAbsent) {
        return set(key, encode(value), expiration, onlyIfAbsent);
    }

    public boolean set(String key, byte[] value, Duration expiration, boolean onlyIfAbsent) {
        var watch = new StopWatch();
        boolean updated = false;
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            int length = 3 + (onlyIfAbsent ? 1 : 0) + (expiration != null ? 2 : 0);
            connection.writeArray(length);
            connection.writeBulkString(SET);
            connection.writeBulkString(encode(key));
            connection.writeBulkString(value);
            if (onlyIfAbsent) connection.writeBulkString(NX);
            if (expiration != null) {
                connection.writeBulkString(EX);
                connection.writeBulkString(encode(expiration.getSeconds()));
            }
            connection.flush();
            String result = connection.readSimpleString();
            updated = "OK".equals(result);
            return updated;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, updated ? 1 : 0);
            logger.debug("set, key={}, value={}, expiration={}, onlyIfAbsent={}, updated={}, elapsed={}", key, new BytesLogParam(value), expiration, onlyIfAbsent, updated, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void expire(String key, Duration expiration) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentCommand(EXPIRE, key, encode(expiration.getSeconds()));
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
    public long del(String... keys) {
        var watch = new StopWatch();
        if (keys.length == 0) throw new Error("keys must not be empty");
        long deletedKeys = 0;
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeysCommand(DEL, keys);
            deletedKeys = connection.readLong();
            return deletedKeys;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, (int) deletedKeys);
            logger.debug("del, keys={}, size={}, deletedKeys={}, elapsed={}", new ArrayLogParam(keys), keys.length, deletedKeys, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public long increaseBy(String key, long increment) {
        var watch = new StopWatch();
        long value = 0;
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentCommand(INCRBY, key, encode(increment));
            value = connection.readLong();
            return value;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, 1);
            logger.debug("increaseBy, key={}, increment={}, returnedValue={}, elapsed={}", key, increment, value, elapsed);
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
        if (keys.length == 0) throw new Error("keys must not be empty");
        Map<String, byte[]> values = Maps.newHashMapWithExpectedSize(keys.length);
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeysCommand(MGET, keys);
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
            ActionLogContext.track("redis", elapsed, values.size(), 0);
            logger.debug("mget, keys={}, size={}, returnedValues={}, elapsed={}", new ArrayLogParam(keys), keys.length, new BytesValueMapLogParam(values), elapsed);
            checkSlowOperation(elapsed);
        }
    }

    @Override
    public void multiSet(Map<String, String> values) {
        var watch = new StopWatch();
        if (values.isEmpty()) throw new Error("values must not be empty");
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeArray(1 + values.size() * 2);
            connection.writeBulkString(MSET);
            for (Map.Entry<String, String> entry : values.entrySet()) {
                connection.writeBulkString(encode(entry.getKey()));
                connection.writeBulkString(encode(entry.getValue()));
            }
            connection.flush();
            connection.readSimpleString();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            int size = values.size();
            ActionLogContext.track("redis", elapsed, 0, size);
            logger.debug("mset, values={}, size={}, elapsed={}", new MapLogParam(values), size, elapsed);
            checkSlowOperation(elapsed);
        }
    }

    public void multiSet(Map<String, byte[]> values, Duration expiration) {
        var watch = new StopWatch();
        if (values.isEmpty()) throw new Error("values must not be empty");
        byte[] expirationValue = encode(expiration.getSeconds());
        int size = values.size();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            for (Map.Entry<String, byte[]> entry : values.entrySet()) { // redis doesn't support mset with expiration, here to use pipeline
                connection.writeArray(5);
                connection.writeBulkString(SET);
                connection.writeBulkString(encode(entry.getKey()));
                connection.writeBulkString(entry.getValue());
                connection.writeBulkString(EX);
                connection.writeBulkString(expirationValue);
            }
            connection.flush();
            connection.readAll(size);
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, size);
            logger.debug("mset, values={}, size={}, expiration={}, elapsed={}", new BytesValueMapLogParam(values), size, expiration, elapsed);
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
                connection.writeArray(6);
                connection.writeBulkString(SCAN);
                connection.writeBulkString(encode(cursor));
                connection.writeBulkString(MATCH);
                connection.writeBulkString(encode(pattern));
                connection.writeBulkString(COUNT);
                connection.writeBulkString(batchSize);
                connection.flush();
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
