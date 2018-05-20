package core.framework.module;

import core.framework.impl.web.service.WebServiceClient;
import org.mockito.Mockito;

/**
 * @author neo
 */
public class TestAPIConfig extends APIConfig {
    @Override
    <T> T createWebServiceClient(Class<T> serviceInterface, WebServiceClient webServiceClient) {
        return Mockito.mock(serviceInterface);
    }
}
