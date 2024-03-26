package core.framework.internal.redis;

import core.framework.internal.log.filter.ArrayLogParam;
import core.framework.internal.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisSortedSet;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import static core.framework.internal.redis.Protocol.Command.ZADD;
import static core.framework.internal.redis.Protocol.Command.ZINCRBY;
import static core.framework.internal.redis.Protocol.Command.ZPOPMIN;
import static core.framework.internal.redis.Protocol.Command.ZRANGE;
import static core.framework.internal.redis.Protocol.Command.ZREM;
import static core.framework.internal.redis.Protocol.Keyword.BYSCORE;
import static core.framework.internal.redis.Protocol.Keyword.LIMIT;
import static core.framework.internal.redis.Protocol.Keyword.NX;
import static core.framework.internal.redis.Protocol.Keyword.WITHSCORES;
import static core.framework.internal.redis.RedisEncodings.decode;
import static core.framework.internal.redis.RedisEncodings.encode;
import static core.framework.internal.redis.RedisEncodings.validate;

/**
 * @author tempo
 */
public class RedisSortedSetImpl implements RedisSortedSet {
    private final Logger logger = LoggerFactory.getLogger(RedisSortedSetImpl.class);
    private final RedisImpl redis;

    RedisSortedSetImpl(RedisImpl redis) {
        this.redis = redis;
    }

    @Override
    public int add(String key, Map<String, Long> values, boolean onlyIfAbsent) {
        var watch = new StopWatch();
        validate("key", key);
        validate("values", values);
        int added = 0;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            int length = 2 + values.size() * 2;
            if (onlyIfAbsent) length++;
            connection.writeArray(length);
            connection.writeBlobString(ZADD);
            connection.writeBlobString(encode(key));
            if (onlyIfAbsent) connection.writeBlobString(NX);
            for (Map.Entry<String, Long> entry : values.entrySet()) {
                connection.writeBlobString(encode(entry.getValue()));
                connection.writeBlobString(encode(entry.getKey()));
            }
            connection.flush();
            added = (int) connection.readLong();
            return added;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("zadd, key={}, values={}, onlyIfAbsent={}, added={}, elapsed={}", key, values, onlyIfAbsent, added, elapsed);
            ActionLogContext.track("redis", elapsed, 0, added);
        }
    }

    @Override
    public long increaseScoreBy(String key, String value, long increment) {
        var watch = new StopWatch();
        validate("key", key);
        validate("value", value);
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        long score = 0;
        try {
            RedisConnection connection = item.resource;
            connection.writeArray(4);
            connection.writeBlobString(ZINCRBY);
            connection.writeBlobString(encode(key));
            connection.writeBlobString(encode(increment));
            connection.writeBlobString(encode(value));
            connection.flush();
            score = (long) Double.parseDouble(decode(connection.readBlobString()));
            return score;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("zincrby, key={}, value={}, increment={}, score={}, elapsed={}", key, value, increment, score, elapsed);
            ActionLogContext.track("redis", elapsed, 0, 1);
        }
    }

    @Override
    public Map<String, Long> range(String key, long start, long stop) {
        var watch = new StopWatch();
        validate("key", key);
        Map<String, Long> values = null;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeArray(5);
            connection.writeBlobString(ZRANGE);
            connection.writeBlobString(encode(key));
            connection.writeBlobString(encode(start));
            connection.writeBlobString(encode(stop));
            connection.writeBlobString(WITHSCORES);
            connection.flush();
            Object[] response = connection.readArray();
            values = valuesWithScores(response);
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("zrange, key={}, start={}, stop={}, returnedValues={}, elapsed={}", key, start, stop, values, elapsed);
            int readEntries = values == null ? 0 : values.size();
            ActionLogContext.track("redis", elapsed, readEntries, 0);
        }
    }

    @Override
    public Map<String, Long> rangeByScore(String key, long minScore, long maxScore, long limit) {
        var watch = new StopWatch();
        validate("key", key);
        if (limit == 0) throw new Error("limit must not be 0");
        if (maxScore < minScore) throw new Error("maxScore must be larger than minScore");

        Map<String, Long> values = null;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            Object[] response = rangeByScore(connection, key, minScore, maxScore, limit);
            values = valuesWithScores(response);
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("zrangeByScore, key={}, minScore={}, maxScore={}, limit={}, returnedValues={}, elapsed={}", key, minScore, maxScore, limit, values, elapsed);
            int readEntries = values == null ? 0 : values.size();
            ActionLogContext.track("redis", elapsed, readEntries, 0);
        }
    }

    private Object[] rangeByScore(RedisConnection connection, String key, long minScore, long maxScore, long limit) throws IOException {
        connection.writeArray(9);
        connection.writeBlobString(ZRANGE);
        connection.writeBlobString(encode(key));
        connection.writeBlobString(encode(minScore));
        connection.writeBlobString(encode(maxScore));
        connection.writeBlobString(BYSCORE);
        connection.writeBlobString(WITHSCORES);
        connection.writeBlobString(LIMIT);
        connection.writeBlobString(encode(0));
        connection.writeBlobString(encode(limit));
        connection.flush();
        return connection.readArray();
    }

    @SuppressWarnings("PMD.ExceptionAsFlowControl") // intentional, simplest way to unify control flow
    @Override
    public Map<String, Long> popByScore(String key, long minScore, long maxScore, long limit) {
        var watch = new StopWatch();
        validate("key", key);
        if (limit == 0) throw new Error("limit must not be 0");
        if (maxScore < minScore) throw new Error("stop must be larger than start");

        int fetchedEntries = 0;
        Map<String, Long> values = null;
        int size = 0;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            Object[] response = rangeByScore(connection, key, minScore, maxScore, -1);
            if (response.length % 2 != 0) throw new IOException("unexpected length of array, length=" + response.length);
            values = Maps.newLinkedHashMapWithExpectedSize(response.length / 2);
            fetchedEntries = response.length / 2;
            for (int i = 0; i < response.length; i += 2) {
                byte[] value = (byte[]) response[i];
                connection.writeKeyArgumentCommand(ZREM, key, value);
                long removed = connection.readLong();
                if (removed == 1L) {
                    values.put(decode(value), (long) Double.parseDouble(decode((byte[]) response[i + 1])));
                    size++;
                    if (limit > 0 && size >= limit) break;
                }
            }
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("popByScore, key={}, minScore={}, maxScore={}, limit={}, returnedValues={}, size={}, elapsed={}", key, minScore, maxScore, limit, values, size, elapsed);
            ActionLogContext.track("redis", elapsed, fetchedEntries, size);
        }
    }

    @Override
    public Map<String, Long> popMin(String key, long limit) {
        var watch = new StopWatch();
        validate("key", key);
        if (limit <= 0) throw new Error("limit must be greater than 0");
        Map<String, Long> values = null;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentCommand(ZPOPMIN, key, encode(limit));
            Object[] response = connection.readArray();
            values = valuesWithScores(response);
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("zpopmin, key={}, limit={}, returnedValues={}, elapsed={}", key, limit, values, elapsed);
            int readEntries = values == null ? 0 : values.size();
            ActionLogContext.track("redis", elapsed, readEntries, 0);
        }
    }

    @Override
    public long remove(String key, String... values) {
        var watch = new StopWatch();
        validate("key", key);
        validate("values", values);
        long removedValues = 0;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentsCommand(ZREM, key, values);
            removedValues = connection.readLong();
            return removedValues;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            int size = values.length;
            logger.debug("zrem, key={}, values={}, size={}, removedValues={}, elapsed={}", key, new ArrayLogParam(values), size, removedValues, elapsed);
            ActionLogContext.track("redis", elapsed, 0, size);
        }
    }

    private Map<String, Long> valuesWithScores(Object[] response) throws IOException {
        if (response.length % 2 != 0) throw new IOException("unexpected length of array, length=" + response.length);
        Map<String, Long> values = Maps.newLinkedHashMapWithExpectedSize(response.length / 2);
        for (int i = 0; i < response.length; i += 2) {
            values.put(decode((byte[]) response[i]), (long) Double.parseDouble(decode((byte[]) response[i + 1])));
        }
        return values;
    }
}
