package core.framework.web.service;

/**
 * @author neo
 */
public interface WebServiceClientProxy {    // all dynamic webservice clients will impl this interface to support extension
    void intercept(WebServiceClientInterceptor interceptor);
}
