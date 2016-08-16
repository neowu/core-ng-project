package core.framework.impl.web.session;

import core.framework.api.redis.Redis;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public class RedisSessionStore implements SessionStore {
    private final Redis redis;

    public RedisSessionStore(Redis redis) {
        this.redis = redis;
    }

    @Override
    public Map<String, String> getAndRefresh(String sessionId, Duration sessionTimeout) {
        String key = sessionKey(sessionId);
        Map<String, String> data = redis.hash().getAll(key);
        if (data.isEmpty()) return null;

        redis.expire(key, Duration.ofSeconds(sessionTimeout.getSeconds()));
        return data;
    }

    @Override
    public void save(String sessionId, Map<String, String> sessionData, Duration sessionTimeout) {
        String key = sessionKey(sessionId);
        redis.hash().multiSet(key, sessionData);
        redis.expire(key, Duration.ofSeconds(sessionTimeout.getSeconds()));
    }

    @Override
    public void clear(String sessionId) {
        String key = sessionKey(sessionId);
        redis.del(key);
    }

    private String sessionKey(String sessionId) {
        return "session:" + sessionId;
    }
}
