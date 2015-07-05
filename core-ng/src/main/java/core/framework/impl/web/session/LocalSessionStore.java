package core.framework.impl.web.session;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class LocalSessionStore implements SessionStore {
    private final Logger logger = LoggerFactory.getLogger(LocalSessionStore.class);

    private final Map<String, SessionValue> values = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        scheduler.scheduleWithFixedDelay(this::cleanup, 30, 30, TimeUnit.MINUTES);
        logger.info("local session cleaner started");
    }

    public void shutdown() {
        logger.info("shutdown local session cleaner");
        scheduler.shutdown();
    }

    void cleanup() {
        Thread.currentThread().setName("local-session-cleaner");
        logger.info("clean up expired sessions");
        Instant now = Instant.now();
        values.forEach((id, session) -> {
            if (now.isAfter(session.expiredTime)) {
                values.remove(id);
            }
        });
    }

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

    static class SessionValue {
        final Instant expiredTime;
        final Map<String, String> data;

        SessionValue(Instant expiredTime, Map<String, String> data) {
            this.expiredTime = expiredTime;
            this.data = data;
        }
    }
}
