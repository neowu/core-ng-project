package core.framework.module;

import core.framework.impl.web.service.WebServiceClient;
import core.framework.web.service.WebServiceClientInterceptor;

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
