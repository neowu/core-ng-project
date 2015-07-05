package core.framework.api.web.client;

import core.framework.api.http.HTTPRequest;

/**
 * @author neo
 */
public interface WebServiceRequestSigner {
    void sign(HTTPRequest request);
}
