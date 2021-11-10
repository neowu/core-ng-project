package core.framework.internal.web.site;

import core.framework.http.ContentType;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class WebSecurityInterceptorTest {
    @Mock
    Request request;
    @Mock
    Response response;
    private WebSecurityInterceptor interceptor;

    @BeforeEach
    void createWebSecurityInterceptor() {
        interceptor = new WebSecurityInterceptor();
    }

    @Test
    void appendSecurityHeaders() {
        interceptor.contentSecurityPolicy = "default-src 'self';";

        when(response.contentType()).thenReturn(Optional.of(ContentType.TEXT_HTML));

        interceptor.appendSecurityHeaders(response);
        verify(response).header(eq("Strict-Transport-Security"), anyString());
        verify(response).header("Content-Security-Policy", interceptor.contentSecurityPolicy);
        verify(response).header(eq("X-XSS-Protection"), anyString());
        verify(response).header(eq("X-Content-Type-Options"), anyString());
        verify(response).header(eq("X-Frame-Options"), anyString());
    }

    @Test
    void skipCSP() {
        interceptor.contentSecurityPolicy = null;

        when(response.contentType()).thenReturn(Optional.of(ContentType.TEXT_HTML));

        interceptor.appendSecurityHeaders(response);
        verify(response, never()).header(eq("Content-Security-Policy"), anyString());
    }

    @Test
    void redirectURL() {
        when(request.hostname()).thenReturn("host");
        when(request.path()).thenReturn("/path");
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("key1", "value1");
        queryParams.put("key2", "value2");
        queryParams.put("key3", "");
        when(request.queryParams()).thenReturn(queryParams);

        String redirectURL = interceptor.redirectURL(request);
        assertThat(redirectURL).isEqualTo("https://host/path?key1=value1&key2=value2&key3=");
    }
}
