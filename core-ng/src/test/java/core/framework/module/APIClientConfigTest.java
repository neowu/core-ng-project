package core.framework.module;

import core.framework.internal.web.service.TestWebServiceClientInterceptor;
import core.framework.web.service.WebServiceClientProxy;
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
        var interceptor = new TestWebServiceClientInterceptor();
        config.intercept(interceptor);
        verify(proxy).intercept(interceptor);
    }
}
