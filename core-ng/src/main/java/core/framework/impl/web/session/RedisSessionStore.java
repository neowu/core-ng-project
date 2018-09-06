package core.framework.impl.web.session;

import core.framework.crypto.Hash;
import core.framework.redis.Redis;
import core.framework.util.Lists;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            Map<String, String> sessionValues = redis.hash().getAll(key);
            if (sessionValues.isEmpty()) return null;
            redis.expire(key, sessionTimeout);
            return sessionValues;
        } catch (Exception e) {    // gracefully handle invalid data in redis, either legacy old format value, or invalid value inserted manually
            logger.warn("failed to get redis session values", e);
            return null;
        }
    }

    @Override
    public void save(String sessionId, Map<String, String> values, Set<String> changedFields, Duration sessionTimeout) {
        String key = sessionKey(sessionId);

        List<String> deletedFields = Lists.newArrayList();
        Map<String, String> updatedValues = Maps.newHashMap();
        for (String changedSessionField : changedFields) {
            String value = values.get(changedSessionField);
            if (value == null) deletedFields.add(changedSessionField);
            else updatedValues.put(changedSessionField, value);
        }
        if (!deletedFields.isEmpty()) redis.hash().del(key, deletedFields.toArray(new String[0]));
        if (!updatedValues.isEmpty()) redis.hash().multiSet(key, updatedValues);
        redis.expire(key, sessionTimeout);
    }

    @Override
    public void invalidate(String sessionId) {
        String key = sessionKey(sessionId);
        redis.del(key);
    }

    String sessionKey(String sessionId) {
        return "session:" + Hash.sha256Hex(sessionId);
    }
}
