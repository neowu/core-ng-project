package core.framework.api.web.service;

import core.framework.api.http.HTTPRequest;

/**
 * @author neo
 */
public interface WebServiceRequestInterceptor {
    void intercept(HTTPRequest request);
}
