package core.framework.internal.web.session;

import core.framework.crypto.Hash;
import core.framework.internal.redis.RedisException;
import core.framework.redis.Redis;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static core.framework.log.Markers.errorCode;

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
    public Map<String, String> getAndRefresh(String sessionId, String domain, Duration timeout) {
        String key = sessionKey(sessionId, domain);
        try {
            Map<String, String> sessionValues = redis.hash().getAll(key);
            if (sessionValues.isEmpty()) return null;
            redis.expire(key, timeout);
            return sessionValues;
        } catch (RedisException e) {
            // gracefully handle invalid data in redis, either legacy old format value, or invalid value/key type inserted manually,
            logger.warn(errorCode("INVALID_SESSION_VALUE"), "failed to get redis session values", e);
            return null;
        }
    }

    @Override
    public void save(String sessionId, String domain, Map<String, String> values, Set<String> changedFields, Duration timeout) {
        String key = sessionKey(sessionId, domain);

        List<String> deletedFields = Lists.newArrayList();
        Map<String, String> updatedValues = Maps.newHashMap();
        for (String changedSessionField : changedFields) {
            String value = values.get(changedSessionField);
            if (value == null) deletedFields.add(changedSessionField);
            else updatedValues.put(changedSessionField, value);
        }
        if (!deletedFields.isEmpty()) redis.hash().del(key, deletedFields.toArray(new String[0]));
        if (!updatedValues.isEmpty()) redis.hash().multiSet(key, updatedValues);
        redis.expire(key, timeout);
    }

    @Override
    public void invalidate(String sessionId, String domain) {
        String key = sessionKey(sessionId, domain);
        redis.del(key);
    }

    // use naive solution, generally invalidate by key/value is used to kick out login user, it happens rarely and will be handled by message handler which is in background
    @Override
    public void invalidateByKey(String key, String value) {
        redis.forEach("session:*", sessionKey -> {
            String valueInSession = redis.hash().get(sessionKey, key);
            if (Strings.equals(value, valueInSession)) {
                redis.del(sessionKey);
            }
        });
    }

    // make sure sessionId can only be used for specific domain, as different webapp may share one session redis, this way to prevent session hijacking by manually reuse sessionId from one site to another
    String sessionKey(String sessionId, String domain) {
        return "session:" + Hash.sha256Hex(domain + ":" + sessionId);
    }
}
