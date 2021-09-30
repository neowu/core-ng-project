package core.framework.internal.web.session;


import core.framework.util.Maps;
import core.framework.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class LocalSessionStore implements SessionStore {
    final Map<String, SessionValue> values = Maps.newConcurrentHashMap();

    private final Logger logger = LoggerFactory.getLogger(LocalSessionStore.class);

    @Override
    public Map<String, String> getAndRefresh(String sessionId, String domain, Duration timeout) {
        SessionValue sessionValue = values.get(sessionId);
        if (sessionValue == null) return null;

        if (Instant.now().isAfter(sessionValue.expirationTime)) {
            values.remove(sessionId);
            return null;
        }

        Map<String, String> sessionValues = sessionValue.values;
        values.put(sessionId, new SessionValue(expirationTime(timeout), sessionValues));
        return sessionValues;
    }

    @Override
    public void save(String sessionId, String domain, Map<String, String> values, Set<String> changedFields, Duration timeout) {
        Map<String, String> updatedValues = Maps.newHashMapWithExpectedSize(values.size());
        values.forEach((field, value) -> {
            if (value != null) updatedValues.put(field, value);
        });
        this.values.put(sessionId, new SessionValue(expirationTime(timeout), updatedValues));
    }

    @Override
    public void invalidate(String sessionId, String domain) {
        values.remove(sessionId);
    }

    @Override
    public void invalidateByKey(String key, String value) {
        values.values().removeIf(session -> Strings.equals(value, session.values.get(key)));
    }

    private Instant expirationTime(Duration sessionTimeout) {
        return Instant.now().plus(sessionTimeout);
    }

    public void cleanup() {
        logger.info("cleanup local session store");
        Instant now = Instant.now();
        values.values().removeIf(session -> now.isAfter(session.expirationTime));
    }

    static class SessionValue {
        final Instant expirationTime;
        final Map<String, String> values;

        SessionValue(Instant expirationTime, Map<String, String> values) {
            this.expirationTime = expirationTime;
            this.values = values;
        }
    }
}
