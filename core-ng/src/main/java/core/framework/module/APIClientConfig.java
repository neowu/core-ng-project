package core.framework.module;

import core.framework.internal.web.service.WebServiceClientProxy;
import core.framework.web.service.WebServiceClientInterceptor;

/**
 * @author neo
 */
public final class APIClientConfig {     // returned from APIConfig, must be public to be called
    private final WebServiceClientProxy client;

    APIClientConfig(WebServiceClientProxy client) {
        this.client = client;
    }

    public void intercept(WebServiceClientInterceptor interceptor) {
        client.intercept(interceptor);
    }
}
