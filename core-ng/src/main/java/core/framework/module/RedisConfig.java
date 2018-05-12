package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.redis.RedisImpl;
import core.framework.impl.resource.PoolMetrics;
import core.framework.redis.Redis;

import java.time.Duration;

/**
 * @author neo
 */
public class RedisConfig {
    protected final Redis redis;
    private final ModuleContext context;
    private String host;

    RedisConfig(ModuleContext context) {
        this.context = context;
        redis = createRedis();
        context.beanFactory.bind(Redis.class, null, redis);
    }

    void validate() {
        if (host == null) throw new Error("redis().host() must be configured");
    }

    Redis createRedis() {
        Redis redis = new RedisImpl("redis");
        context.shutdownHook.add(((RedisImpl) redis)::close);
        context.backgroundTask().scheduleWithFixedDelay(((RedisImpl) redis).pool::refresh, Duration.ofMinutes(5));
        context.stat.metrics.add(new PoolMetrics(((RedisImpl) redis).pool));
        return redis;
    }

    public void host(String host) {
        setHost(host);
        this.host = host;
    }

    void setHost(String host) {
        RedisImpl redis = (RedisImpl) this.redis;
        redis.host = host;
    }

    public void poolSize(int minSize, int maxSize) {
        ((RedisImpl) redis).pool.size(minSize, maxSize);
    }

    public void slowOperationThreshold(Duration threshold) {
        ((RedisImpl) redis).slowOperationThreshold(threshold);
    }

    public void timeout(Duration timeout) {
        ((RedisImpl) redis).timeout(timeout);
    }
}
