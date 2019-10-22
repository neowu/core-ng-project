package core.framework.module;

import core.framework.internal.web.service.WebServiceClientProxy;
import core.framework.web.service.WebServiceClientInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class APIClientConfigTest {
    private APIClientConfig config;
    private WebServiceClientProxy proxy;

    @BeforeEach
    void createAPIClientConfig() {
        proxy = mock(WebServiceClientProxy.class);
        config = new APIClientConfig(proxy);
    }

    @Test
    void intercept() {
        WebServiceClientInterceptor interceptor = request -> {
        };
        config.intercept(interceptor);
        verify(proxy).intercept(interceptor);
    }
}
