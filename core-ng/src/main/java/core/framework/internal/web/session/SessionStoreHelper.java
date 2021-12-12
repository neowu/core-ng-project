package core.framework.internal.web.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
class SessionStoreHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionStoreHelper.class);

    static Duration timeout(Map<String, String> values, Duration defaultTimeout) {
        String timeoutValue = values.get(SessionImpl.TIMEOUT_FIELD);
        try {
            if (timeoutValue != null) return Duration.ofSeconds(Long.parseLong(timeoutValue));
            return defaultTimeout;
        } catch (NumberFormatException e) {
            // ignore in case session values contains poison value
            LOGGER.warn("invalid timeout value from session store, value={}", timeoutValue);
            return defaultTimeout;
        }
    }
}
