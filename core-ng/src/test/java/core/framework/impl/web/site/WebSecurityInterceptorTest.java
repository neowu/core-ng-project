package core.framework.impl.web.site;

import core.framework.util.Maps;
import core.framework.web.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
    void contentSecurityPolicy() {
        assertThat(interceptor.contentSecurityPolicy()).isEqualTo("default-src https://*; img-src https://* data:; object-src 'none'; frame-src 'none';");
        assertThat(interceptor.contentSecurityPolicy("*")).isEqualTo("default-src https://*; img-src https://* data:; object-src 'none'; frame-src 'none';");
        assertThat(interceptor.contentSecurityPolicy("https://cdn", "https://ga")).isEqualTo("default-src 'self' https://cdn https://ga; img-src 'self' https://cdn https://ga data:; object-src 'none'; frame-src 'none';");
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
