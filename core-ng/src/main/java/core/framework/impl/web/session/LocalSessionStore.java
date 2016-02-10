package core.framework.impl.web.session;


import core.framework.api.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class LocalSessionStore implements SessionStore {
    private final Logger logger = LoggerFactory.getLogger(LocalSessionStore.class);
    private final Map<String, SessionValue> values = Maps.newConcurrentHashMap();

    @Override
    public Map<String, String> getAndRefresh(String sessionId, Duration sessionTimeout) {
        SessionValue sessionValue = values.get(sessionId);
        if (sessionValue == null) return null;

        if (Instant.now().isAfter(sessionValue.expiredTime)) {
            values.remove(sessionId);
            return null;
        }

        Map<String, String> data = sessionValue.data;
        values.put(sessionId, new SessionValue(expirationTime(sessionTimeout), data));
        return data;
    }

    @Override
    public void save(String sessionId, Map<String, String> sessionData, Duration sessionTimeout) {
        values.put(sessionId, new SessionValue(expirationTime(sessionTimeout), sessionData));
    }

    @Override
    public void clear(String sessionId) {
        values.remove(sessionId);
    }

    private Instant expirationTime(Duration sessionTimeout) {
        return Instant.now().plus(sessionTimeout);
    }

    public void cleanup() {
        logger.info("cleanup local session store");
        Instant now = Instant.now();
        values.forEach((id, session) -> {
            if (now.isAfter(session.expiredTime)) {
                values.remove(id);
            }
        });
    }

    static class SessionValue {
        final Instant expiredTime;
        final Map<String, String> data;

        SessionValue(Instant expiredTime, Map<String, String> data) {
            this.expiredTime = expiredTime;
            this.data = data;
        }
    }
}
