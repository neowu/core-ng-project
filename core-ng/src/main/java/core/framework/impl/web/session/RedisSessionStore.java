package core.framework.impl.web.session;

import core.framework.api.redis.Redis;
import core.framework.api.util.JSON;
import core.framework.api.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public class RedisSessionStore implements SessionStore {
    private final Logger logger = LoggerFactory.getLogger(RedisSessionStore.class);

    private final Redis redis;

    public RedisSessionStore(Redis redis) {
        this.redis = redis;
    }

    @Override
    public Map<String, String> getAndRefresh(String sessionId, Duration sessionTimeout) {
        String key = sessionKey(sessionId);
        try {
            String value = redis.get(key);
            if (value == null) return null;
            logger.debug("[session] value={}", value);
            redis.expire(key, sessionTimeout);
            return decode(value);
        } catch (Exception e) {    // gracefully handle invalid data in redis, either legacy old format value, or invalid value inserted manually
            logger.warn("failed to get redis session", e);
            return null;
        }
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
