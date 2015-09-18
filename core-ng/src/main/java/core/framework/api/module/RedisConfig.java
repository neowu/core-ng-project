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

    public RedisConfig(ModuleContext context) {
        this.context = context;

        if (context.beanFactory.registered(Redis.class, null)) {
            redis = context.beanFactory.bean(Redis.class, null);
        } else {
            if (context.test) {
                redis = null;
                context.beanFactory.bind(Redis.class, null, new MockRedis());
            } else {
                redis = new RedisImpl();
                context.shutdownHook.add(redis::close);
                context.scheduler().addTrigger(new FixedRateTrigger("refresh-redis-pool", new RefreshPoolJob(redis.pool), Duration.ofMinutes(5)));
                context.beanFactory.bind(Redis.class, null, redis);
            }
        }
    }

    public void host(String host) {
        if (context.test) {
            logger.info("skip host during test");
        } else {
            redis.host(host);
        }
    }

    public void poolSize(int minSize, int maxSize) {
        if (!context.test) {
            redis.pool.size(minSize, maxSize);
        }
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        if (!context.test) {
            redis.slowQueryThreshold(slowQueryThreshold);
        }
    }

    public void timeout(Duration timeout) {
        if (!context.test) {
            redis.timeout(timeout);
        }
    }
}
