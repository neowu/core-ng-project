package core.framework.internal.web.controller;

import core.framework.web.CookieSpec;
import core.framework.web.Response;
import org.junit.jupiter.api.AfterEach;
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
    void createWebContextImpl() {
        context = new WebContextImpl();
        context.initialize(null);
    }

    @AfterEach
    void cleanup() {
        context.cleanup();
    }

    @Test
    void get() {
        context.put("key", "value");

        String value = (String) context.get("key");
        assertThat(value).isEqualTo("value");
    }

    @Test
    void responseCookie() {
        Response response = mock(Response.class);
        var spec = new CookieSpec("test");

        context.responseCookie(spec, null);
        context.handleResponse(response);

        verify(response).cookie(spec, null);
    }
}
