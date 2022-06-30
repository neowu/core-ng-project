package core.framework.module;

import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import core.framework.internal.redis.RedisImpl;
import core.framework.internal.resource.PoolMetrics;
import core.framework.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public class RedisConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    private ModuleContext context;
    private Redis redis;
    private String name;
    private String host;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
        this.name = name;
        redis = createRedis();
        context.beanFactory.bind(Redis.class, name, redis);
    }

    @Override
    protected void validate() {
        if (host == null) throw new Error("redis host must be configured, name=" + name);
    }

    Redis createRedis() {
        logger.info("create redis client, name={}", name);
        var redis = new RedisImpl("redis" + (name == null ? "" : "-" + name));
        context.shutdownHook.add(ShutdownHook.STAGE_6, timeout -> redis.close());
        context.backgroundTask().scheduleWithFixedDelay(redis.pool::refresh, Duration.ofMinutes(5));
        context.collector.metrics.add(new PoolMetrics(redis.pool));
        return redis;
    }

    public void host(String host) {
        setHost(host);
        this.host = host;
    }

    void setHost(String host) {
        RedisImpl redis = (RedisImpl) this.redis;
        redis.host(host);
        context.probe.hostURIs.add(host);
    }

    public void password(String password) {
        RedisImpl redis = (RedisImpl) this.redis;
        redis.password(password);
    }

    public void poolSize(int minSize, int maxSize) {
        ((RedisImpl) redis).pool.size(minSize, maxSize);
    }

    public void timeout(Duration timeout) {
        ((RedisImpl) redis).timeout(timeout);
    }

    public Redis client() {
        return redis;
    }
}
