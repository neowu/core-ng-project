package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.redis.RedisImpl;
import core.framework.impl.resource.PoolMetrics;
import core.framework.redis.Redis;

import java.time.Duration;

/**
 * @author neo
 */
public final class RedisConfig {
    private final ModuleContext context;
    private final Redis redis;
    private String host;

    RedisConfig(ModuleContext context) {
        this.context = context;
        redis = createRedis();
    }

    void validate() {
        if (host == null) throw new Error("redis().host() must be configured");
    }

    private Redis createRedis() {
        Redis redis;
        if (context.isTest()) {
            redis = context.mockFactory.create(Redis.class);
        } else {
            redis = new RedisImpl("redis");
            context.shutdownHook.add(((RedisImpl) redis)::close);
            context.backgroundTask().scheduleWithFixedDelay(((RedisImpl) redis).pool::refresh, Duration.ofMinutes(5));
            context.stat.metrics.add(new PoolMetrics(((RedisImpl) redis).pool));
        }
        context.beanFactory.bind(Redis.class, null, redis);
        return redis;
    }

    public void host(String host) {
        if (!context.isTest()) {
            RedisImpl redis = (RedisImpl) this.redis;
            redis.host = host;
        }
        this.host = host;
    }

    public void poolSize(int minSize, int maxSize) {
        if (!context.isTest()) {
            ((RedisImpl) redis).pool.size(minSize, maxSize);
        }
    }

    public void slowOperationThreshold(Duration threshold) {
        if (!context.isTest()) {
            ((RedisImpl) redis).slowOperationThreshold(threshold);
        }
    }

    public void timeout(Duration timeout) {
        if (!context.isTest()) {
            ((RedisImpl) redis).timeout(timeout);
        }
    }
}
