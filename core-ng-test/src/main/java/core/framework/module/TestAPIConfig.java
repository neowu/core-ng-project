package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.service.WebServiceClient;
import org.mockito.Mockito;

/**
 * @author neo
 */
public class TestAPIConfig extends APIConfig {
    TestAPIConfig(ModuleContext context) {
        super(context);
    }

    @Override
    <T> T createWebServiceClient(Class<T> serviceInterface, WebServiceClient webServiceClient) {
        return Mockito.mock(serviceInterface);
    }
}
