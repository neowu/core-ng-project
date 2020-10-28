package core.framework.internal.redis;

import core.framework.internal.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisSortedSet;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

import static core.framework.internal.redis.Protocol.Command.ZADD;
import static core.framework.internal.redis.Protocol.Command.ZRANGEBYSCORE;
import static core.framework.internal.redis.Protocol.Command.ZREM;
import static core.framework.internal.redis.Protocol.Keyword.INF_MIN;
import static core.framework.internal.redis.Protocol.Keyword.LIMIT;
import static core.framework.internal.redis.Protocol.Keyword.NX;
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
    public boolean push(String key, long score, String value, boolean onlyIfAbsent) {
        var watch = new StopWatch();
        boolean updated = false;

        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            int length = 4 + (onlyIfAbsent ? 1 : 0);
            connection.writeArray(length);
            connection.writeBlobString(ZADD);
            connection.writeBlobString(encode(key));
            connection.writeBlobString(encode(score));
            connection.writeBlobString(encode(value));
            if (onlyIfAbsent) connection.writeBlobString(NX);

            connection.flush();
            String result = connection.readSimpleString();
            updated = "OK".equals(result);
            return updated;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, updated ? 1 : 0);
            logger.debug("zadd, key={}, value={}, onlyIfAbsent={}, updated={}, elapsed={}", key, value, onlyIfAbsent, updated, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public String popByScoreCap(String key, long maxScore) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        boolean fetched = false;
        String value = null;

        try {
            RedisConnection connection = item.resource;
            int length = 7;
            connection.writeArray(length);
            connection.writeBlobString(ZRANGEBYSCORE);
            connection.writeBlobString(encode(key));
            connection.writeBlobString(INF_MIN);
            connection.writeBlobString(encode(maxScore));
            connection.writeBlobString(LIMIT);
            connection.writeBlobString(encode(0L));
            connection.writeBlobString(encode(1L));
            connection.flush();

            byte[] response = connection.readBlobString();
            if (response == null) {
                return null;
            }

            fetched = true;
            connection.writeKeyArgumentCommand(ZREM, key, response);
            long l = connection.readLong();
            if (1L == l) {
                value = decode(response);
            }
            return value;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, fetched ? 1 : 0, value != null ? 1 : 0);
            logger.debug("zrangebyscore, zrem, key={}, returnedValues={}, elapsed={}", key, value, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }
}
