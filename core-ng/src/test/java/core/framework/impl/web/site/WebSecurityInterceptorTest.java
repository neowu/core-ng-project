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
        interceptor = new WebSecurityInterceptor(null);
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
