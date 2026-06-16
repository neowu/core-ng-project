package core.framework.internal.web.session;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public interface SessionStore {
    @Nullable
    Map<String, String> getAndRefresh(String sessionId, String domain, Duration timeout);

    void save(String sessionId, String domain, Map<String, @Nullable String> values, Set<String> changedFields, Duration timeout);

    void invalidate(String sessionId, String domain);

    void invalidateByKey(String key, String value);
}
