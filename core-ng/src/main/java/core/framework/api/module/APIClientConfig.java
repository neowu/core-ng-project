package core.framework.api.module;

import core.framework.api.web.service.WebServiceClientInterceptor;
import core.framework.impl.web.service.WebServiceClient;

/**
 * @author neo
 */
public final class APIClientConfig {     // returned from APIConfig, must be public to be called
    private final WebServiceClient client;

    APIClientConfig(WebServiceClient client) {
        this.client = client;
    }

    public void intercept(WebServiceClientInterceptor interceptor) {
        client.intercept(interceptor);
    }
}
