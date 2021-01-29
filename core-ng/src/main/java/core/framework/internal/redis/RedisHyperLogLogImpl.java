package core.framework.internal.redis;

import core.framework.internal.log.filter.ArrayLogParam;
import core.framework.internal.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisHyperLogLog;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

import static core.framework.internal.redis.Protocol.Command.PFADD;
import static core.framework.internal.redis.Protocol.Command.PFCOUNT;
import static core.framework.internal.redis.Protocol.Command.PFMERGE;
import static core.framework.internal.redis.RedisEncodings.validate;

/**
 * @author tempo
 */
public class RedisHyperLogLogImpl implements RedisHyperLogLog {
    private final Logger logger = LoggerFactory.getLogger(RedisHyperLogLogImpl.class);
    private final RedisImpl redis;

    public RedisHyperLogLogImpl(RedisImpl redis) {
        this.redis = redis;
    }

    @Override
    public boolean add(String key, String... values) {
        var watch = new StopWatch();
        validate("key", key);
        validate("values", values);
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentsCommand(PFADD, key, values);
            return connection.readLong() == 1;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, values.length);
            logger.debug("pfadd, key={}, values={}, size={}, elapsed={}", key, new ArrayLogParam(values), values.length, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public long count(String... keys) {
        var watch = new StopWatch();
        validate("keys", keys);
        long size = 0;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeysCommand(PFCOUNT, keys);
            size = connection.readLong();
            return size;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 1, 0);
            logger.debug("pfcount, keys={}, size={}, elapsed={}", keys, size, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }

    @Override
    public long merge(String destinationKey, String... sourceKeys) {
        var watch = new StopWatch();
        validate("destinationKey", destinationKey);
        validate("sourceKeys", sourceKeys);
        long size = 0;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentsCommand(PFMERGE, destinationKey, sourceKeys);
            size = connection.readLong();
            return size;
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 1, sourceKeys.length);
            logger.debug("pfmerge, destinationKey={}, sourceKeys={}, size={}, elapsed={}", destinationKey, new ArrayLogParam(sourceKeys), size, elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }
}
