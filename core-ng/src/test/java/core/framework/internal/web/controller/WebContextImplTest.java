package core.framework.internal.web.controller;

import core.framework.web.CookieSpec;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class WebContextImplTest {
    private WebContextImpl context;

    @BeforeEach
    void createWebContext() {
        context = new WebContextImpl();
    }

    @Test
    void get() {
        context.run(mock(Request.class), () -> {
            context.put("key", "value");

            String value = (String) context.get("key");
            assertThat(value).isEqualTo("value");
        });
    }

    @Test
    void request() {
        context.run(mock(Request.class), () -> {
            Request request = context.request();

            assertThat(request).isNotNull();
        });
    }

    @Test
    void responseCookie() {
        Response response = mock(Response.class);
        var spec = new CookieSpec("test");

        context.run(mock(Request.class), () -> {
            context.responseCookie(spec, null);
            context.handleResponse(response);
            verify(response).cookie(spec, null);
        });
    }
}
