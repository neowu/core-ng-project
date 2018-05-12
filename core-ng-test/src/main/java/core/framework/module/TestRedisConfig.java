package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.redis.Redis;
import core.framework.test.redis.MockRedis;

import java.time.Duration;

/**
 * @author neo
 */
public class TestRedisConfig extends RedisConfig {
    TestRedisConfig(ModuleContext context) {
        super(context);
    }

    @Override
    Redis createRedis() {
        return new MockRedis();
    }

    @Override
    void setHost(String host) {
    }

    @Override
    public void poolSize(int minSize, int maxSize) {
    }

    @Override
    public void slowOperationThreshold(Duration threshold) {
    }

    @Override
    public void timeout(Duration timeout) {
    }
}
