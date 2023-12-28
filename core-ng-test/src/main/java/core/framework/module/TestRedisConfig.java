package core.framework.module;

import core.framework.redis.Redis;
import core.framework.test.redis.MockRedis;

import java.time.Duration;

/**
 * @author neo
 */
public class TestRedisConfig extends RedisConfig {
    @Override
    Redis createRedis() {
        return new MockRedis();
    }

    @Override
    void setHost(String host) {
    }

    @Override
    public void password(String password) {
    }

    @Override
    public void poolSize(int minSize, int maxSize) {
    }

    @Override
    public void timeout(Duration timeout) {
    }
}
