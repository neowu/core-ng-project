package core.framework.api.module;

import core.framework.api.web.service.WebServiceRequestSigner;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.service.WebServiceClient;

/**
 * @author neo
 */
final class WebServiceClientConfig {
    private final ModuleContext context;
    private final WebServiceClient client;

    WebServiceClientConfig(ModuleContext context, WebServiceClient client) {
        this.context = context;
        this.client = client;
    }

    public void signBy(WebServiceRequestSigner signer) {
        if (!context.isTest()) {
            client.signBy(signer);
        }
    }
}
