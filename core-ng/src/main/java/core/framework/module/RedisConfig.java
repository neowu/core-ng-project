package core.framework.module;

import core.framework.impl.module.Config;
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
    private final State state;

    RedisConfig(ModuleContext context) {
        this.context = context;
        state = context.config.state("redis", State::new);

        if (state.redis == null) {
            state.redis = createRedis();
        }
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
            RedisImpl redis = (RedisImpl) state.redis;
            redis.host = host;
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

    public static class State implements Config.State {
        String host;
        Redis redis;

        @Override
        public void validate() {
            if (host == null) throw new Error("redis().host() must be configured");
        }
    }
}
