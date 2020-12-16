package core.framework.internal.redis;

import core.framework.internal.log.filter.BytesLogParam;
import core.framework.internal.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

import static core.framework.internal.redis.Protocol.Command.PUBLISH;

/**
 * @author neo
 */
public class RedisPubSub {
    private final Logger logger = LoggerFactory.getLogger(RedisPubSub.class);
    private final RedisImpl redis;

    public RedisPubSub(RedisImpl redis) {
        this.redis = redis;
    }

    public void publish(String channel, byte[] message) {
        var watch = new StopWatch();
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeKeyArgumentCommand(PUBLISH, channel, message);
            connection.readLong();
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            ActionLogContext.track("redis", elapsed, 0, 1);
            logger.debug("publish, channel={}, message={}, elapsed={}", channel, new BytesLogParam(message), elapsed);
            redis.checkSlowOperation(elapsed);
        }
    }
}
