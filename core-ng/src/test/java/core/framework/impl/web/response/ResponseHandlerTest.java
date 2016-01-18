package core.framework.impl.web.response;

import core.framework.api.web.CookieSpec;
import io.undertow.server.handlers.CookieImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class ResponseHandlerTest {
    ResponseHandler responseHandler;

    @Before
    public void createResponseHandler() {
        responseHandler = new ResponseHandler(null, null);
    }

    @Test
    public void cookie() {
        CookieImpl cookie = responseHandler.cookie(new CookieSpec("test").secure(), "1=2");
        assertEquals("test", cookie.getName());
        assertEquals("1%3D2", cookie.getValue());
    }
}