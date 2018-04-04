package core.framework.impl.web.response;

import core.framework.web.CookieSpec;
import io.undertow.server.handlers.CookieImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class ResponseHandlerTest {
    private ResponseHandler responseHandler;

    @BeforeEach
    void createResponseHandler() {
        responseHandler = new ResponseHandler(null, null);
    }

    @Test
    void cookie() {
        CookieImpl cookie = responseHandler.cookie(new CookieSpec("test").secure(), "1=2");
        assertEquals("test", cookie.getName());
        assertEquals("1%3D2", cookie.getValue());
    }
}
