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
    private final State state;

    public RedisConfig(ModuleContext context) {
        this.context = context;
        state = context.config.redis();

        if (state.redis == null) {
            state.redis = createRedis();
        }
    }

    private Redis createRedis() {
        Redis redis;
        if (context.isTest()) {
            redis = context.mockFactory.create(Redis.class);
        } else {
            redis = new RedisImpl();
            context.shutdownHook.add(((RedisImpl) redis)::close);
            context.backgroundTask().scheduleWithFixedDelay(((RedisImpl) redis).pool::refresh, Duration.ofMinutes(5));
        }
        context.beanFactory.bind(Redis.class, null, redis);
        return redis;
    }

    public void host(String host) {
        if (!context.isTest()) {
            ((RedisImpl) state.redis).host(host);
        }
        state.host = host;
    }

    public void poolSize(int minSize, int maxSize) {
        if (!context.isTest()) {
            ((RedisImpl) state.redis).pool.size(minSize, maxSize);
        }
    }

    public void slowOperationThreshold(Duration threshold) {
        if (!context.isTest()) {
            ((RedisImpl) state.redis).slowOperationThreshold(threshold);
        }
    }

    public void timeout(Duration timeout) {
        if (!context.isTest()) {
            ((RedisImpl) state.redis).timeout(timeout);
        }
    }

    public static class State {
        String host;
        Redis redis;

        public void validate() {
            if (host == null) throw new Error("redis().host() must be configured");
        }
    }
}
