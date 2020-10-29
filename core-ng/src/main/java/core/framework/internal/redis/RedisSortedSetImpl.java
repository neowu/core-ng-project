package core.framework.internal.redis;

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
import static core.framework.internal.redis.Protocol.Command.ZRANGE;
import static core.framework.internal.redis.Protocol.Command.ZRANGEBYSCORE;
import static core.framework.internal.redis.Protocol.Command.ZREM;
import static core.framework.internal.redis.Protocol.Keyword.LIMIT;
import static core.framework.internal.redis.Protocol.Keyword.NX;
import static core.framework.internal.redis.Protocol.Keyword.WITHSCORES;
import static core.framework.internal.redis.RedisEncodings.decode;
import static core.framework.internal.redis.RedisEncodings.encode;

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
    public boolean add(String key, String value, long score, boolean onlyIfAbsent) {
        var watch = new StopWatch();
        int added = 0;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            int length = onlyIfAbsent ? 5 : 4;
            connection.writeArray(length);
            connection.writeBlobString(ZADD);
            connection.writeBlobString(encode(key));
            if (onlyIfAbsent) connection.writeBlobString(NX);
            connection.writeBlobString(encode(score));
            connection.writeBlobString(encode(value));
            connection.flush();
            added = (int) connection.readLong();
            return added > 0;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, added);
            logger.debug("zadd, key={}, value={}, score={}, onlyIfAbsent={}, added={}, elapsed={}", key, value, score, onlyIfAbsent, added, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public Map<String, Long> range(String key, long start, long stop) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        Map<String, Long> values = null;
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
            if (response.length % 2 != 0) throw new IOException("unexpected length of array, length=" + response.length);
            values = Maps.newLinkedHashMapWithExpectedSize(response.length / 2);
            for (int i = 0; i < response.length; i += 2) {
                values.put(decode((byte[]) response[i]), (long) Double.parseDouble(decode((byte[]) response[i + 1])));
            }
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, values == null ? 0 : values.size(), 0);
            logger.debug("zrange, key={}, start={}, stop={}, returnedValues={}, elapsed={}", key, start, stop, values, elapsed);
        }
    }

    @Override
    public Map<String, Long> popByScore(String key, long minScore, long maxScore, long limit) {
        var watch = new StopWatch();
        if (limit == 0) throw new Error("limit must not be 0");
        if (maxScore < minScore) throw new Error("stop must be larger than start");

        int fetchedEntries = 0;
        Map<String, Long> values = null;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeArray(8);
            connection.writeBlobString(ZRANGEBYSCORE);
            connection.writeBlobString(encode(key));
            connection.writeBlobString(encode(minScore));
            connection.writeBlobString(encode(maxScore));
            connection.writeBlobString(WITHSCORES);
            connection.writeBlobString(LIMIT);
            connection.writeBlobString(encode(0));
            connection.writeBlobString(encode(limit));
            connection.flush();
            Object[] response = connection.readArray();
            if (response.length % 2 != 0) throw new IOException("unexpected length of array, length=" + response.length);
            values = Maps.newLinkedHashMapWithExpectedSize(response.length / 2);
            fetchedEntries = response.length / 2;
            for (int i = 0; i < response.length; i += 2) {
                connection.writeKeyArgumentCommand(ZREM, key, (byte[]) response[i]);
                long removed = connection.readLong();
                if (removed == 1L) {
                    values.put(decode((byte[]) response[i]), (long) Double.parseDouble(decode((byte[]) response[i + 1])));
                }
            }
            return values;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            int size = values == null ? 0 : values.size();
            ActionLogContext.track("redis", elapsed, fetchedEntries, size);
            logger.debug("popByScore, key={}, start={}, stop={}, poppedValues={}, size={}, elapsed={}", key, minScore, maxScore, values, size, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }
}
