package core.framework.module;

import core.framework.internal.web.service.WebServiceClient;
import core.framework.web.service.WebServiceClientProxy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * @author neo
 */
public class TestAPIConfig extends APIConfig {
    @Override
    <T> T createWebServiceClient(Class<T> serviceInterface, WebServiceClient webServiceClient) {
        return mock(serviceInterface, withSettings().extraInterfaces(WebServiceClientProxy.class));
    }
}
