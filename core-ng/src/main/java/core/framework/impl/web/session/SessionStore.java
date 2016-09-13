package core.framework.impl.web.session;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
interface SessionStore {
    Map<String, String> getAndRefresh(String sessionId, Duration sessionTimeout);

    void save(String sessionId, Map<String, String> sessionData, Duration sessionTimeout);

    void invalidate(String sessionId);
}
