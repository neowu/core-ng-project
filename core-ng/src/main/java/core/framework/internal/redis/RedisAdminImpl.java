package core.framework.internal.redis;

import core.framework.internal.resource.PoolItem;
import core.framework.log.ActionLogContext;
import core.framework.redis.RedisAdmin;
import core.framework.util.Maps;
import core.framework.util.StopWatch;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.StringTokenizer;

import static core.framework.internal.redis.Protocol.Command.INFO;
import static core.framework.internal.redis.RedisEncodings.decode;

/**
 * @author neo
 */
public class RedisAdminImpl implements RedisAdmin {
    private final Logger logger = LoggerFactory.getLogger(RedisAdminImpl.class);
    private final RedisImpl redis;

    public RedisAdminImpl(RedisImpl redis) {
        this.redis = redis;
    }

    @Override
    public Map<String, String> info() {
        var watch = new StopWatch();
        String value = null;
        PoolItem<RedisConnection> item = redis.pool.borrowItem();
        try {
            RedisConnection connection = item.resource;
            connection.writeCommand(INFO);
            value = decode(connection.readBlobString());
            return parseInfo(value);
        } catch (IOException e) {
            item.broken = true;
            throw new UncheckedIOException(e);
        } finally {
            redis.pool.returnItem(item);
            long elapsed = watch.elapsed();
            logger.debug("info, returnedValue={}, elapsed={}", value, elapsed);
            ActionLogContext.track("redis", elapsed, 1, 0);
        }
    }

    Map<String, String> parseInfo(String info) {
        Map<String, String> values = Maps.newHashMapWithExpectedSize(128);  // redis 5.x return roughly 122 keys for info
        StringTokenizer tokenizer = new StringTokenizer(info, "\r\n");
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            if (!Strings.startsWith(line, '#')) {
                int index = line.indexOf(':');
                values.put(line.substring(0, index), line.substring(index + 1));
            }
        }
        return values;
    }
}
