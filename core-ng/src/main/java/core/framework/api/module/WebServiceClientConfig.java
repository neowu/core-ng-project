package core.framework.api.module;

import core.framework.api.web.service.WebServiceRequestInterceptor;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.service.WebServiceClient;

/**
 * @author neo
 */
public final class WebServiceClientConfig {     // returned from APIConfig, must be public to be called
    private final ModuleContext context;
    private final WebServiceClient client;

    WebServiceClientConfig(ModuleContext context, WebServiceClient client) {
        this.context = context;
        this.client = client;
    }

    public void intercept(WebServiceRequestInterceptor interceptor) {
        if (!context.isTest()) {
            client.intercept(interceptor);
        }
    }
}
