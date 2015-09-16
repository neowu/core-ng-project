package core.framework.api.module;

import core.framework.api.redis.Redis;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.redis.MockRedis;
import core.framework.impl.redis.RedisImpl;
import core.framework.impl.resource.RefreshPoolJob;
import core.framework.impl.scheduler.FixedRateTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * @author neo
 */
public class RedisConfig {
    private final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    private final ModuleContext context;
    final RedisImpl redis;

    public RedisConfig(ModuleContext context, String name) {
        this.context = context;

        if (context.beanFactory.registered(Redis.class, name)) {
            redis = context.beanFactory.bean(Redis.class, name);
        } else {
            if (context.test) {
                redis = null;
                context.beanFactory.bind(Redis.class, name, new MockRedis());
            } else {
                redis = new RedisImpl();
                context.shutdownHook.add(redis::close);
                String poolName = "redis" + (name == null ? "" : "-" + name);
                redis.pool.name(poolName);
                context.scheduler().addTrigger(new FixedRateTrigger("refresh-" + poolName + "-pool", new RefreshPoolJob(redis.pool), Duration.ofMinutes(5)));
                context.beanFactory.bind(Redis.class, name, redis);
            }
        }
    }

    public RedisConfig host(String host) {
        if (context.test) {
            logger.info("skip host during test");
        } else {
            redis.host(host);
        }
        return this;
    }

    public RedisConfig poolSize(int minSize, int maxSize) {
        if (!context.test) {
            redis.pool.size(minSize, maxSize);
        }
        return this;
    }

    public RedisConfig slowQueryThreshold(Duration slowQueryThreshold) {
        if (!context.test) {
            redis.slowQueryThreshold(slowQueryThreshold);
        }
        return this;
    }

    public RedisConfig timeout(Duration timeout) {
        if (!context.test) {
            redis.timeout(timeout);
        }
        return this;
    }
}
