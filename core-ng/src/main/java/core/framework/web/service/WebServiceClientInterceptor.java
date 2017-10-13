package core.framework.web.service;

import core.framework.http.HTTPRequest;

/**
 * @author neo
 */
public interface WebServiceClientInterceptor {
    void intercept(HTTPRequest request);
}
