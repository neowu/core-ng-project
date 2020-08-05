package core.framework.module;

import core.framework.internal.web.service.TestWebServiceClientInterceptor;
import core.framework.web.service.WebServiceClientProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class APIClientConfigTest {
    @Mock
    WebServiceClientProxy proxy;
    private APIClientConfig config;

    @BeforeEach
    void createAPIClientConfig() {
        config = new APIClientConfig(proxy);
    }

    @Test
    void intercept() {
        var interceptor = new TestWebServiceClientInterceptor();
        config.intercept(interceptor);
        verify(proxy).intercept(interceptor);
    }
}
