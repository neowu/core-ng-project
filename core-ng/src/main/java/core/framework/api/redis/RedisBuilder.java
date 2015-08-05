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

    public RedisBuilder() {
        config.setJmxEnabled(false);
        poolSize(5, 50);    // default optimization for AWS medium/large instances
        config.setMinEvictableIdleTimeMillis(Duration.ofMinutes(30).toMillis());    // close if connection idles for more than 30 min
    }

    public RedisBuilder host(String host) {
        this.host = host;
        return this;
    }

    public RedisBuilder poolSize(int minSize, int maxSize) {
        config.setMinIdle(minSize);
        config.setMaxIdle(maxSize);
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
        JedisPool pool = new JedisPool(config, host, Protocol.DEFAULT_PORT, (int) timeout.toMillis(), null, Protocol.DEFAULT_DATABASE, null);
        return new Redis(pool, slowQueryThreshold.toMillis());
    }
}
