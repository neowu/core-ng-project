package core.framework.impl.web;

import core.framework.impl.web.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author neo
 */
class HTTPLoggerTest {
    private HTTPLogger httpLogger;
    private SessionManager sessionManager;

    @BeforeEach
    void createHTTPLogger() {
        sessionManager = new SessionManager();
        httpLogger = new HTTPLogger(sessionManager);
    }

    @Test
    void maskCookieValue() {
        String maskedValue = httpLogger.maskCookieValue(sessionManager.sessionId.name, "value");
        assertNotEquals("value", maskedValue);
    }
}
