package core.framework.module;

import core.framework.internal.web.service.WebServiceClient;
import core.framework.internal.web.service.WebServiceClientProxy;
import org.mockito.Mockito;

import static org.mockito.Mockito.withSettings;

/**
 * @author neo
 */
public class TestAPIConfig extends APIConfig {
    @Override
    <T> T createWebServiceClient(Class<T> serviceInterface, WebServiceClient webServiceClient) {
        return Mockito.mock(serviceInterface, withSettings().extraInterfaces(WebServiceClientProxy.class));
    }
}
