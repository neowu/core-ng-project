package core.framework.impl.web.session;

import core.framework.api.redis.Redis;
import core.framework.api.util.JSON;
import core.framework.api.util.Types;

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
        String value = redis.get(key);
        if (value == null) return null;
        redis.expire(key, sessionTimeout);
        return decode(value);
    }

    @Override
    public void save(String sessionId, Map<String, String> sessionData, Duration sessionTimeout) {
        String key = sessionKey(sessionId);
        redis.set(key, encode(sessionData), sessionTimeout);
    }

    Map<String, String> decode(String value) {
        return JSON.fromJSON(Types.map(String.class, String.class), value);
    }

    String encode(Map<String, String> sessionData) {
        return JSON.toJSON(sessionData);
    }

    @Override
    public void invalidate(String sessionId) {
        String key = sessionKey(sessionId);
        redis.del(key);
    }

    private String sessionKey(String sessionId) {
        return "session:" + sessionId;
    }
}
