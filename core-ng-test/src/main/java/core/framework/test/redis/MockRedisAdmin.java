package core.framework.test.redis;

import core.framework.redis.RedisAdmin;

import java.util.Map;

/**
 * @author neo
 */
public class MockRedisAdmin implements RedisAdmin {
    @Override
    public Map<String, String> info() {
        return Map.of();
    }
}
