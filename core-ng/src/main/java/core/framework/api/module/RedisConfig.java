package core.framework.api.module;

import core.framework.api.redis.Redis;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.redis.RedisImpl;

import java.time.Duration;

/**
 * @author neo
 */
public final class RedisConfig {
    private final ModuleContext context;
    private final Redis redis;

    public RedisConfig(ModuleContext context) {
        this.context = context;

        if (context.beanFactory.registered(Redis.class, null)) {
            redis = context.beanFactory.bean(Redis.class, null);
        } else {
            if (context.isTest()) {
                redis = context.mockFactory.create(Redis.class);
            } else {
                RedisImpl redis = new RedisImpl();
                context.shutdownHook.add(redis::close);
                context.backgroundTask().scheduleWithFixedDelay(redis.pool::refresh, Duration.ofMinutes(5));
                this.redis = redis;
            }
            context.beanFactory.bind(Redis.class, null, redis);
        }
    }

    public void host(String host) {
        if (!context.isTest()) {
            ((RedisImpl) redis).host(host);
        }
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
