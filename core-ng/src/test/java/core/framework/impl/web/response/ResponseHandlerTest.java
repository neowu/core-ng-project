package core.framework.impl.web.response;

import core.framework.impl.web.session.SessionManager;
import core.framework.web.CookieSpec;
import io.undertow.server.handlers.CookieImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author neo
 */
class ResponseHandlerTest {
    private ResponseHandler responseHandler;
    private SessionManager sessionManager;

    @BeforeEach
    void createResponseHandler() {
        sessionManager = new SessionManager();
        responseHandler = new ResponseHandler(null, null, sessionManager);
    }

    @Test
    void cookie() {
        CookieImpl cookie = responseHandler.cookie(new CookieSpec("test").secure(), "1=2");
        assertEquals("test", cookie.getName());
        assertEquals("1%3D2", cookie.getValue());
    }

    @Test
    void maskCookieValue() {
        String maskedValue = responseHandler.maskCookieValue(sessionManager.sessionId.name, "value");
        assertNotEquals("value", maskedValue);
    }
}
