package core.framework.impl.web.session;


import core.framework.util.Maps;
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
    public Map<String, String> getAndRefresh(String sessionId, Duration sessionTimeout) {
        SessionValue sessionValue = values.get(sessionId);
        if (sessionValue == null) return null;

        if (Instant.now().isAfter(sessionValue.expirationTime)) {
            values.remove(sessionId);
            return null;
        }

        Map<String, String> sessionValues = sessionValue.values;
        values.put(sessionId, new SessionValue(expirationTime(sessionTimeout), sessionValues));
        return sessionValues;
    }

    @Override
    public void save(String sessionId, Map<String, String> values, Set<String> changedFields, Duration sessionTimeout) {
        Map<String, String> updatedValues = Maps.newHashMapWithExpectedSize(values.size());
        values.forEach((field, value) -> {
            if (value != null) updatedValues.put(field, value);
        });
        this.values.put(sessionId, new SessionValue(expirationTime(sessionTimeout), updatedValues));
    }

    @Override
    public void invalidate(String sessionId) {
        values.remove(sessionId);
    }

    private Instant expirationTime(Duration sessionTimeout) {
        return Instant.now().plus(sessionTimeout);
    }

    public void cleanup() {
        logger.info("cleanup local session store");
        Instant now = Instant.now();
        values.forEach((id, session) -> {
            if (now.isAfter(session.expirationTime)) {
                values.remove(id);
            }
        });
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
