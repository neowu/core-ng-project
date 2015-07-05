package core.framework.api.module;

import core.framework.api.web.client.WebServiceRequestSigner;

/**
 * @author neo
 */
public interface WebServiceClientConfig {
    void signBy(WebServiceRequestSigner signer);
}
