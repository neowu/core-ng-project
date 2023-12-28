package core.framework.internal.redis;

import core.framework.internal.log.filter.ArrayLogParam;
import core.framework.internal.log.filter.BytesLogParam;
import core.framework.internal.log.filter.BytesMapLogParam;
import core.framework.internal.log.filter.FieldMapLogParam;
import core.framework.internal.resource.Pool;
import core.framework.internal.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.Redis;
import core.framework.redis.RedisAdmin;
import core.framework.redis.RedisHash;
import core.framework.redis.RedisHyperLogLog;
import core.framework.redis.RedisList;
import core.framework.redis.RedisSet;
import core.framework.redis.RedisSortedSet;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import static core.framework.internal.redis.Protocol.Command.DEL;
import static core.framework.internal.redis.Protocol.Command.GET;
import static core.framework.internal.redis.Protocol.Command.INCRBY;
import static core.framework.internal.redis.Protocol.Command.MGET;
import static core.framework.internal.redis.Protocol.Command.MSET;
import static core.framework.internal.redis.Protocol.Command.PEXPIRE;
import static core.framework.internal.redis.Protocol.Command.PTTL;
import static core.framework.internal.redis.Protocol.Command.SCAN;
import static core.framework.internal.redis.Protocol.Command.SET;
import static core.framework.internal.redis.Protocol.Keyword.COUNT;
import static core.framework.internal.redis.Protocol.Keyword.MATCH;
import static core.framework.internal.redis.Protocol.Keyword.NX;
import static core.framework.internal.redis.Protocol.Keyword.PX;
import static core.framework.internal.redis.RedisEncodings.decode;
import static core.framework.internal.redis.RedisEncodings.encode;
import static core.framework.internal.redis.RedisEncodings.validate;

/**
 * @author neo
 */
public final class RedisImpl implements Redis {
    final RedisConnectionFactory connectionFactory = new RedisConnectionFactory();

    private final Logger logger = LoggerFactory.getLogger(RedisImpl.class);
    private final RedisSet redisSet = new RedisSetImpl(this);
    private final RedisHash redisHash = new RedisHashImpl(this);
    private final RedisList redisList = new RedisListImpl(this);
    private final RedisSortedSet redisSortedSet = new RedisSortedSetImpl(this);
    private final RedisHyperLogLog redisHyperLogLog = new RedisHyperLogLogImpl(this);
    private final RedisAdmin redisAdmin = new RedisAdminImpl(this);
    private final String name;
    public Pool<RedisConnection> pool;

    public RedisImpl(String name) {
        this.name = name;
        pool = new Pool<>(connectionFactory, name);
        pool.size(5, 50);
        pool.maxIdleTime = Duration.ofMinutes(30);
        pool.checkoutTimeout(Duration.ofSeconds(5));
    }

    public void host(String host) {
        connectionFactory.host = new RedisHost(host);
    }

    public void password(String password) {
        connectionFactory.password = password;
    }

    public void timeout(Duration timeout) {
        connectionFactory.timeoutInMs = (int) timeout.toMillis();
        pool.checkoutTimeout(timeout);
    }

    public void close() {
        logger.info("close redis client, name={}, host={}", name, connectionFactory.host);
        pool.close();
    }

    @Override
    public String get(String key) {
        validate("key", key);   // only validate on interface methods, internal usage will be checked by caller
        return decode(getBytes(key));
    }

    public byte[] getBytes(String key) {
        var watch = new StopWatch();
        byte[] value = null;
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyCommand(GET, key);
            value = connection.readBlobString();
            return value;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("get, key={}, returnedValue={}, elapsed={}", key, new BytesLogParam(value), elapsed);
            ActionLogContext.track("redis", elapsed, 1, 0);
        }
    }

    @Override
    public RedisSet set() {
        return redisSet;
    }

    @Override
    public boolean set(String key, String value, Duration expiration, boolean onlyIfAbsent) {
        validate("key", key);
        validate("value", value);
        return set(key, encode(value), expiration, onlyIfAbsent);
    }

    public boolean set(String key, byte[] value, Duration expiration, boolean onlyIfAbsent) {
        var watch = new StopWatch();
        byte[] expirationValue = expiration == null ? null : expirationValue(expiration);
        boolean updated = false;
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            int length = 3 + (onlyIfAbsent ? 1 : 0) + (expiration != null ? 2 : 0);
            connection.writeArray(length);
            connection.writeBlobString(SET);
            connection.writeBlobString(encode(key));
            connection.writeBlobString(value);
            if (onlyIfAbsent) connection.writeBlobString(NX);
            if (expiration != null) {
                connection.writeBlobString(PX);
                connection.writeBlobString(expirationValue);
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
            logger.debug("set, key={}, value={}, expiration={}, onlyIfAbsent={}, updated={}, elapsed={}", key, new BytesLogParam(value), expiration, onlyIfAbsent, updated, elapsed);
            int writeEntries = updated ? 1 : 0;
            ActionLogContext.track("redis", elapsed, 0, writeEntries);
        }
    }

    @Override
    public void expire(String key, Duration expiration) {
        var watch = new StopWatch();
        validate("key", key);
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentCommand(PEXPIRE, key, encode(expiration.toMillis()));    // PEXPIRE accepts zero and negative ttl
            connection.readLong();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("pexpire, key={}, expiration={}, elapsed={}", key, expiration, elapsed);
            ActionLogContext.track("redis", elapsed, 0, 1);
        }
    }

    @Override
    public long del(String... keys) {
        var watch = new StopWatch();
        validate("keys", keys);
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
            logger.debug("del, keys={}, size={}, deletedKeys={}, elapsed={}", new ArrayLogParam(keys), keys.length, deletedKeys, elapsed);
            ActionLogContext.track("redis", elapsed, 0, (int) deletedKeys);
        }
    }

    @Override
    public long increaseBy(String key, long increment) {
        var watch = new StopWatch();
        validate("key", key);
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
            logger.debug("incrby, key={}, increment={}, returnedValue={}, elapsed={}", key, increment, value, elapsed);
            ActionLogContext.track("redis", elapsed, 0, 1);
        }
    }

    @Override
    public Map<String, String> multiGet(String... keys) {
        Map<String, byte[]> values = multiGetBytes(keys);
        Map<String, String> result = Maps.newLinkedHashMapWithExpectedSize(values.size());
        for (Map.Entry<String, byte[]> entry : values.entrySet()) {
            result.put(entry.getKey(), decode(entry.getValue()));
        }
        return result;
    }

    public Map<String, byte[]> multiGetBytes(String... keys) {
        var watch = new StopWatch();
        validate("keys", keys);
        Map<String, byte[]> values = Maps.newLinkedHashMapWithExpectedSize(keys.length);
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
            logger.debug("mget, keys={}, size={}, returnedValues={}, elapsed={}", new ArrayLogParam(keys), keys.length, new BytesMapLogParam(values), elapsed);
            ActionLogContext.track("redis", elapsed, values.size(), 0);
        }
    }

    @Override
    public void multiSet(Map<String, String> values) {
        var watch = new StopWatch();
        validate("values", values);
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeArray(1 + values.size() * 2);
            connection.writeBlobString(MSET);
            for (Map.Entry<String, String> entry : values.entrySet()) {
                connection.writeBlobString(encode(entry.getKey()));
                connection.writeBlobString(encode(entry.getValue()));
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
            logger.debug("mset, values={}, size={}, elapsed={}", new FieldMapLogParam(values), size, elapsed);
            ActionLogContext.track("redis", elapsed, 0, size);
        }
    }

    public void multiSet(Map<String, byte[]> values, Duration expiration) {
        var watch = new StopWatch();
        validate("values", values);
        byte[] expirationValue = expirationValue(expiration);
        int size = values.size();
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            for (Map.Entry<String, byte[]> entry : values.entrySet()) { // redis doesn't support mset with expiration, here to use pipeline
                connection.writeArray(5);
                connection.writeBlobString(SET);
                connection.writeBlobString(encode(entry.getKey()));
                connection.writeBlobString(entry.getValue());
                connection.writeBlobString(PX);
                connection.writeBlobString(expirationValue);
            }
            connection.flush();
            connection.readAll(size);
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("set, values={}, size={}, expiration={}, elapsed={}", new BytesMapLogParam(values), size, expiration, elapsed);
            ActionLogContext.track("redis", elapsed, 0, size);
        }
    }

    @Override
    public RedisHash hash() {
        return redisHash;
    }

    @Override
    public RedisList list() {
        return redisList;
    }

    @Override
    public RedisSortedSet sortedSet() {
        return redisSortedSet;
    }

    @Override
    public void forEach(String pattern, Consumer<String> consumer) {
        var watch = new StopWatch();
        if (pattern == null) throw new Error("pattern must not be null");
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
                connection.writeBlobString(SCAN);
                connection.writeBlobString(encode(cursor));
                connection.writeBlobString(MATCH);
                connection.writeBlobString(encode(pattern));
                connection.writeBlobString(COUNT);
                connection.writeBlobString(batchSize);
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
            logger.debug("scan, pattern={}, returnedKeys={}, redisTook={}, elapsed={}", pattern, returnedKeys, redisTook, elapsed);
            ActionLogContext.track("redis", redisTook, returnedKeys, 0);
        }
    }

    @Override
    public RedisAdmin admin() {
        return redisAdmin;
    }

    @Override
    public RedisHyperLogLog hyperLogLog() {
        return redisHyperLogLog;
    }

    public long[] expirationTime(String... keys) {
        var watch = new StopWatch();
        int size = keys.length;
        long[] expirationTimes = null;
        PoolItem<RedisConnection> item = pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            for (String key : keys) {
                connection.writeArray(2);
                connection.writeBlobString(PTTL);
                connection.writeBlobString(encode(key));
            }
            connection.flush();
            Object[] results = connection.readAll(size);
            expirationTimes = new long[size];
            for (int i = 0; i < results.length; i++) {
                Long result = (Long) results[i];
                expirationTimes[i] = result;
            }
            return expirationTimes;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("pttl,  keys={}, size={}, returnedValues={}, elapsed={}", new ArrayLogParam(keys), size, expirationTimes, elapsed);
            ActionLogContext.track("redis", elapsed, size, 0);
        }
    }

    private byte[] expirationValue(Duration expiration) {
        long expirationTime = expiration.toMillis();
        if (expirationTime <= 0) throw new Error("expiration time must be longer than 0ms");
        return encode(expirationTime);
    }
}
