package core.framework.impl.web.site;

import core.framework.http.ContentType;
import core.framework.util.Maps;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class WebSecurityInterceptorTest {
    private WebSecurityInterceptor interceptor;

    @BeforeEach
    void createWebSecurityInterceptor() {
        interceptor = new WebSecurityInterceptor();
    }

    @Test
    void appendSecurityHeaders() {
        interceptor.contentSecurityPolicy = "default-src 'self';";

        Response response = mock(Response.class);
        when(response.contentType()).thenReturn(Optional.of(ContentType.TEXT_HTML));

        interceptor.appendSecurityHeaders(response);
        verify(response).header(eq("Strict-Transport-Security"), anyString());
        verify(response).header("Content-Security-Policy", interceptor.contentSecurityPolicy);
        verify(response).header(eq("X-XSS-Protection"), anyString());
        verify(response).header(eq("X-Content-Type-Options"), anyString());
    }

    @Test
    void redirectURL() {
        Request request = mock(Request.class);
        when(request.hostName()).thenReturn("host");
        when(request.path()).thenReturn("/path");
        Map<String, String> queryParams = Maps.newHashMap();
        queryParams.put("key1", "value1");
        queryParams.put("key2", "value2");
        when(request.queryParams()).thenReturn(queryParams);

        String redirectURL = interceptor.redirectURL(request);
        assertThat(redirectURL).isEqualTo("https://host/path?key1=value1&key2=value2");
    }
}
