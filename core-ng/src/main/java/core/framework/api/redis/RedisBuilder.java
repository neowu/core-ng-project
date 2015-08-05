package core.framework.api.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * @author neo
 */
public final class RedisBuilder implements Supplier<Redis> {
    private final Logger logger = LoggerFactory.getLogger(RedisBuilder.class);

    private String host;
    private Duration timeout = Duration.ofSeconds(2);
    private Duration slowQueryThreshold = Duration.ofMillis(100);
    private final JedisPoolConfig config = new JedisPoolConfig();

    public RedisBuilder host(String host) {
        this.host = host;
        return this;
    }

    public RedisBuilder poolSize(int minSize, int maxSize) {
        config.setMinIdle(minSize);
        config.setMaxTotal(maxSize);
        return this;
    }

    public RedisBuilder slowQueryThreshold(Duration slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
        return this;
    }

    public RedisBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public Redis get() {
        logger.info("create redis client, host={}", host);
        config.setJmxEnabled(false);
        JedisPool pool = new JedisPool(config, host, Protocol.DEFAULT_PORT, (int) timeout.toMillis(), null, Protocol.DEFAULT_DATABASE, null);
        return new Redis(pool, slowQueryThreshold.toMillis());
    }
}
