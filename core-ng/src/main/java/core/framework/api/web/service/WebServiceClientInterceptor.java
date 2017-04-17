package core.framework.api.web.service;

import core.framework.api.http.HTTPRequest;

/**
 * @author neo
 */
public interface WebServiceClientInterceptor {
    void intercept(HTTPRequest request);
}
