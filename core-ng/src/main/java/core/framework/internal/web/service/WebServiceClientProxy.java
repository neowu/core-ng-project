package core.framework.internal.web.service;

import core.framework.web.service.WebServiceClientInterceptor;

/**
 * @author neo
 */
public interface WebServiceClientProxy {    // all dynamic webservice clients will impl this interface to support extension
    void intercept(WebServiceClientInterceptor interceptor);
}
