package core.framework.internal.web.response;

import core.framework.web.CookieSpec;
import io.undertow.server.handlers.CookieImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ResponseHandlerTest {
    private ResponseHandler responseHandler;

    @BeforeEach
    void createResponseHandler() {
        responseHandler = new ResponseHandler(null, null, null);
    }

    @Test
    void cookie() {
        CookieImpl cookie = responseHandler.cookie(new CookieSpec("test").secure().sameSite(), "1=2");
        assertThat(cookie.getName()).isEqualTo("test");
        assertThat(cookie.getValue()).isEqualTo("1%3D2");
        assertThat(cookie.getSameSiteMode()).isEqualTo("Lax");
    }

    @Test
    void cookieKey() {
        assertThat(responseHandler.cookieKey(new CookieSpec("test")))
                .isNotEqualTo(responseHandler.cookieKey(new CookieSpec("test").domain("example.com")))
                .isNotEqualTo(responseHandler.cookieKey(new CookieSpec("test").path("/")));
    }
}
