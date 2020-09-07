package core.framework.module;

import core.framework.internal.inject.InjectValidator;
import core.framework.web.service.WebServiceClientInterceptor;
import core.framework.web.service.WebServiceClientProxy;

/**
 * @author neo
 */
public final class APIClientConfig {     // returned from APIConfig, must be public to be called
    private final WebServiceClientProxy client;

    APIClientConfig(WebServiceClientProxy client) {
        this.client = client;
    }

    public void intercept(WebServiceClientInterceptor interceptor) {
        new InjectValidator(interceptor).validate();
        client.intercept(interceptor);
    }
}
